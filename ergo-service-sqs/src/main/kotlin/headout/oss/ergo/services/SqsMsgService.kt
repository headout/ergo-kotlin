package headout.oss.ergo.services

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.helpers.InMemoryBufferJobResultHandler
import headout.oss.ergo.helpers.JobResultHandler
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.RequestMsg
import headout.oss.ergo.utils.asyncSendDelayed
import headout.oss.ergo.utils.repeatUntilCancelled
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.slf4j.MarkerFactory
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.properties.Delegates

/**
 * Created by shivanshs9 on 28/05/20.
 */
private val logger = KotlinLogging.logger {}

/**
 * Implements Ergo Message Service for SQS FIFO Queues.
 *
 * The service collect task requests from [requestQueueUrl] and pushes the executed job results to [resultQueueUrl]
 * When the workers eventually picks up the task to process, it adds it to [pendingJobs] ensuring the request
 * message's visibility timeout is increased till it's finally finished/errored. At this point, the message is deleted
 * from the [requestQueueUrl] and the job result is handled by the [resultHandler] which finally decides when to push the
 * result message
 *
 * @param sqs sqs client to use to communicate with [requestQueueUrl] and [resultQueueUrl]
 * @param requestQueueUrl FIFO queue URL used to pick up messages as task requests. Messages in this queue must have a
 * "MessageGroupId" and it must be the same as "TaskId"
 * @param resultQueueUrl FIFO queue URL used to push the job results
 * @param defaultVisibilityTimeout visibility timeout (in seconds) assumed for the [requestQueueUrl]. If not provided (or passed null),
 * then it fetches the visibility timeout from the queue attributes of [requestQueueUrl]. If it even fails to fetch, default
 * is assumed to be 30 seconds.
 * @param numWorkers number of child workers to launch to process the tasks (default is 8)
 * @param resultHandler job result handler. Default is an in-memory buffered implementation
 * @param scope coroutine scope of all child coroutines. Default is IO Dispatcher
 */
@ExperimentalCoroutinesApi
class SqsMsgService(
    private val sqs: SqsAsyncClient,
    private val requestQueueUrl: String,
    private val resultQueueUrl: String = requestQueueUrl,
    private val defaultVisibilityTimeout: Long? = null,
    numWorkers: Int = DEFAULT_NUMBER_WORKERS,
    private val resultHandler: JobResultHandler = InMemoryBufferJobResultHandler(MAX_BUFFERED_MESSAGES),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : BaseMsgService<Message>(scope, numWorkers) {
    private var visibilityTimeout by Delegates.notNull<Long>()

    private val defaultPingMessageDelay: Long by lazy { getPingDelay(visibilityTimeout) }
    private val pendingJobs = mutableSetOf<JobId>()

    /**
     * Called on service startup, it initializes required properties like
     * result handler and visibility timeout of the request queue.
     */
    override suspend fun initService() {
        resultHandler.init(this, jobController.parser) { pushResults(it) }
        visibilityTimeout = defaultVisibilityTimeout?.also {
            logger.info { "Using the visibility timeout of $it seconds" }
        } ?: sqs.runCatching {
            logger.info { "Attempting to fetch visibility timeout..." }
            getVisibilityTimeout(requestQueueUrl).also {
                logger.info { "Fetched visibility timeout of $it seconds" }
            }
        }.getOrElse {
            logger.warn(it) { "Failed to fetch!!" }
            DEFAULT_VISIBILITY_TIMEOUT.also {
                logger.info { "Using default visibility timeout of $it seconds" }
            }
        }
    }

    override suspend fun processRequest(request: RequestMsg<Message>): JobResult<*> = try {
        pendingJobs.add(request.jobId)
        jobController.runJob(request.taskId, request.jobId, request.message.body())
    } finally {
        pendingJobs.remove(request.jobId)
    }

    /**
     * Should attempt to process request only if it hasn't been pinged yet.
     * If it has already been pinged and request is processed later, then visibility timeout
     * won't ever increase, thus risking more than 1 consumer processing the task.
     **/
    override fun shouldProcessRequest(request: RequestMsg<Message>): Boolean {
        val diffInMillis = Date().time - request.receiveTimestamp.time
        return diffInMillis <= defaultPingMessageDelay
    }

    override suspend fun collectRequests(): ReceiveChannel<RequestMsg<Message>> =
        produce(Dispatchers.IO, CAPACITY_REQUEST_BUFFER) {
            val receiveRequest = ReceiveMessageRequest.builder()
                .attributeNamesWithStrings(MessageSystemAttributeName.MESSAGE_GROUP_ID.toString())
                .queueUrl(requestQueueUrl)
                .maxNumberOfMessages(MAX_BUFFERED_MESSAGES)
                .waitTimeSeconds(20)
                .build()

            repeatUntilCancelled(BaseMsgService.Companion::collectCaughtExceptions) {
                val messages = sqs.receiveMessage(receiveRequest).await().messages()
                val message = "Received ${messages.size} messages"
                if(messages.isNotEmpty()) logger.info(message) else logger.debug(message)
                for (msg in messages) {
                    val groupId = msg.attributes()[MessageSystemAttributeName.MESSAGE_GROUP_ID]
                        ?: error("Message doesn't have 'MessageGroupId' key!")
                    val requestMsg = RequestMsg(toTaskId(groupId), msg.messageId(), msg).also {
                        logger.debug { "Received request - $it" }
                    }
                    send(requestMsg)
                    asyncSendDelayed(captures, PingMessageCapture(requestMsg), defaultPingMessageDelay, Dispatchers.IO)
                }
            }
        }

    override suspend fun handleCaptures(): Job = launch(Dispatchers.IO) {
        repeatUntilCancelled(BaseMsgService.Companion::collectCaughtExceptions) {
            for (capture in captures) {
                logger.info { "event: '${capture::class.simpleName}', request: '${capture.request}'" }
                when (capture) {
                    is ErrorResultCapture -> handleError(capture)
                    is SuccessResultCapture -> handleSuccess(capture)
                    is RespondResultCapture -> resultHandler.handleResult(capture.result)
                    is PingMessageCapture -> if (pendingJobs.contains(capture.request.jobId)) {
                        logger.debug(MARKER_JOB_BUFFER) { "PING: job '${capture.request.jobId}' still pending (attempt ${capture.attempt})" }
                        val newTimeout =
                            getVisibilityTimeoutForAttempt(visibilityTimeout, capture.attempt)
                        changeVisibilityTimeout(capture.request, newTimeout)
                        asyncSendDelayed(
                            captures,
                            PingMessageCapture(capture.request, capture.attempt + 1),
                            defaultPingMessageDelay,
                            Dispatchers.IO
                        )
                    } else logger.debug(MARKER_JOB_BUFFER) { "PING: jobs - $pendingJobs" }
                }
            }
        }
    }

    private suspend fun handleError(resultCapture: ErrorResultCapture<Message>) = deleteMessage(resultCapture.request)

    private suspend fun handleSuccess(resultCapture: SuccessResultCapture<Message>) =
        deleteMessage(resultCapture.request)

    private suspend fun pushResults(jobResults: List<JobResult<*>>) = launch(Dispatchers.IO) {
        logger.info { "Pushing ${jobResults.size} results!" }
        val msgEntries = jobResults.mapIndexed { index, jobResult ->
            val msgBody = parseResult(jobResult)
            SendMessageBatchRequestEntry.builder()
                .id(index.toString())
                .messageBody(msgBody)
                .messageGroupId(jobResult.taskId)
                .build()
        }
        val sendRequest = SendMessageBatchRequest.builder()
            .queueUrl(resultQueueUrl)
            .entries(msgEntries)
            .build()

        val response = sqs.sendMessageBatch(sendRequest).await()
        response.successful().forEach {
            logger.info {
                "Response message '${it.messageId()}' for '${jobResults[it.id().toInt()]}' successfully sent!"
            }
        }
        response.failed().forEach {
            logger.warn {
                "Response message, indexed '${it.id()}' for '${jobResults[it.id()
                    .toInt()]}', failed to send with code '${it.code()}'!"
            }
        }
    }

    private suspend fun changeVisibilityTimeout(request: RequestMsg<Message>, visibilityTimeout: Long) =
        launch(Dispatchers.IO) {
            logger.info { "Change visibility timeout of message with jobId '${request.jobId}' to $visibilityTimeout seconds" }
            sqs.changeMessageVisibility {
                it.queueUrl(requestQueueUrl)
                it.receiptHandle(request.message.receiptHandle())
                it.visibilityTimeout(visibilityTimeout.toInt())
            }.await()
        }

    private suspend fun deleteMessage(request: RequestMsg<Message>) = launch(Dispatchers.IO) {
        logger.info { "Deleting message with jobId - ${request.jobId}" }
        sqs.deleteMessage {
            it.queueUrl(requestQueueUrl)
            it.receiptHandle(request.message.receiptHandle())
        }.await()
    }

    companion object : TaskServiceConversion {
        const val MAX_BUFFERED_MESSAGES = 10
        private const val MAX_VISIBILITY_TIMEOUT = 43199.toLong()
        val DEFAULT_VISIBILITY_TIMEOUT = TimeUnit.SECONDS.toSeconds(30)

        private val MARKER_JOB_BUFFER = MarkerFactory.getMarker("PendingJob")

        override fun toTaskId(value: String): TaskId = value

        override fun fromTaskId(taskId: TaskId): String = taskId

        fun getPingDelay(visibilityTimeout: Long) = TimeUnit.SECONDS.toMillis(round(visibilityTimeout * 0.75).toLong())

        fun getVisibilityTimeoutForAttempt(visibilityTimeout: Long, attempt: Int) =
            (visibilityTimeout * (attempt + 1)).coerceAtMost(MAX_VISIBILITY_TIMEOUT)
    }
}