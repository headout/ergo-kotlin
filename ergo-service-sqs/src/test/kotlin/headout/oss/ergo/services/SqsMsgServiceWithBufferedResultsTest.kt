package headout.oss.ergo.services

import com.google.common.truth.Truth.assertThat
import headout.oss.ergo.examples.WhyDisKolaveriDi
import headout.oss.ergo.factory.JsonFactory
import headout.oss.ergo.helpers.InMemoryBufferJobResultHandler
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.JobResultMetadata
import headout.oss.ergo.models.RequestMsg
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.junit.Test
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest

/**
 * Created by shivanshs9 on 02/06/20.
 */
private val logger = KotlinLogging.logger {}

@ExperimentalCoroutinesApi
class SqsMsgServiceWithBufferedResultsTest : BaseSqsServiceTest() {
    override fun createSqsMsgService(): SqsMsgService =
        SqsMsgService(sqsClient, QUEUE_URL, defaultVisibilityTimeout = VISIBILITY_TIMEOUT, scope = testScope)

    @Test
    fun sufficientWorkersLaunchedOnStart() {
        msgService.start().apply {
            val workersCount = BaseMsgService.DEFAULT_NUMBER_WORKERS
            assertThat(children.count()).isEqualTo(workersCount)
        }
        val countLauncher = 1
        val countChannels = 1
        val countWorkerSupervisor = 1
        // using buffer result handler by default
        val countBufferResultTimeoutTicker = 2
        assertThat(
            msgService.coroutineContext[Job]?.children?.count() ?: 0
        ).isEqualTo(countChannels + countLauncher + countWorkerSupervisor + countBufferResultTimeoutTicker)
    }

    @Test
    fun invalidMessageWithoutGroupId_HandlesMissingKeyException() {
        mockReceiveMessageResponse()
        msgService.start()
        verify {
            BaseMsgService.collectCaughtExceptions(match {
                it is IllegalStateException && it.message == "Message doesn't have 'MessageGroupId' key!"
            })
        }
    }

    @Test
    fun invalidMessageWithoutBody_VerifyInvalidRequestResult() {
        mockReceiveMessageResponse(taskId = "invalidTask")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_INVALID_REQUEST.code && metadata.error?.message == "message.body() must not be null"
            })
        }
    }

    @Test
    fun validMessageButInvalidTaskId_VerifyTaskNotFoundResult() {
        val taskId = "taskNotFound"
        mockReceiveMessageResponse(taskId = taskId, body = "false")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_NOT_FOUND.code && metadata.error?.message == "Could not find relevant function for the taskId - '$taskId'"
            })
        }
    }

    @Test
    fun whenTaskValidButRequestDataInvalid_VerifyParseErrorResult() {
        val taskId = "xyz.1"
        mockReceiveMessageResponse(taskId = taskId, body = "false")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_PARSE.code
            })
        }
    }

    @Test
    fun whenWorkerEncounteredError_VerifyDeleteMessageRequest() {
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            sqsClient.deleteMessage(match<DeleteMessageRequest> {
                it.receiptHandle() == receiptHandle
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfully_VerifySuccessResult() {
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleSuccess"](match<SuccessResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.SUCCESS.code
            })
        }
    }

//    @ImplicitReflectionSerializer
    @Test
    fun whenTaskRequestValidAndSuspendingTaskAndRanSuccessfully_VerifySuccessResult() {
        val taskId = "suspend.2"
        val body = JsonFactory.json.encodeToString(mapOf("request" to WhyDisKolaveriDi(3)))
        mockReceiveMessageResponse(taskId = taskId, body = body)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT + 2000)
        }
        verify {
            msgService["handleSuccess"](match<SuccessResultCapture<Message>> {
                logger.info("result=${it.result}")
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.SUCCESS.code
            })
        }
    }

    @Test
    fun whenTaskValidButResultAfterVisibilityTimeout_VerifyVisibilityTimeoutIncreased() {
        val taskId = "suspend.long"
        mockReceiveMessageResponse(taskId = taskId, body = "")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(VISIBILITY_TIMEOUT * 1000)
        }
        coVerify {
            msgService["changeVisibilityTimeout"](any<RequestMsg<Message>>(), match<Long> {
                it > VISIBILITY_TIMEOUT
            })
            delay(VISIBILITY_TIMEOUT * 1000)
        }
        verify {
            msgService["handleSuccess"](match<SuccessResultCapture<Message>> {
                logger.info("result=${it.result}")
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.SUCCESS.code
            })
        }
    }

    @Test
    fun whenWorkerEncounteredErrorAndBufferResultsToMax_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenWorkerEncounteredErrorAndBufferResultsInsufficientAndTimeoutHappened_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES - 1
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(InMemoryBufferJobResultHandler.TIMEOUT_RESULT_COLLECTION) // time consuming since timeout value is 2 minutes
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfullyAndBufferResultsToMax_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfullyAndBufferResultsInsufficientAndTimeoutHappened_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES - 1
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(InMemoryBufferJobResultHandler.TIMEOUT_RESULT_COLLECTION) // time consuming since timeout value is 2 minutes
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenPushResultsCalled_BatchSendMessageToSqsQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "noArgWithSerializableResult"
        val body = ""
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
        verify {
            sqsClient.sendMessageBatch(match<SendMessageBatchRequest> {
                val entries = it.entries()
                println(entries)
                it.queueUrl() == QUEUE_URL && entries.size == msgCount
            })
        }
    }
}