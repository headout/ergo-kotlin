package headout.oss.ergo.services

import headout.oss.ergo.exceptions.BaseJobError
import headout.oss.ergo.exceptions.InvalidRequestError
import headout.oss.ergo.exceptions.LibraryInternalError
import headout.oss.ergo.factory.BaseJobController
import headout.oss.ergo.factory.JobController
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.RequestMsg
import headout.oss.ergo.utils.immortalWorkers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.lang.Thread.currentThread

/**
 * Created by shivanshs9 on 28/05/20.
 */
private val logger = KotlinLogging.logger {}

/**
 * Defines common logic for Ergo Message Service and declares required implementation
 *
 * On service start, launches [numWorkers] immortal workers which will eventually pick up and execute tasks
 * The worker coroutines uses a fixed thread pool of [numWorkers] threads as their dispatcher
 *
 * @param numWorkers number of child workers to launch to process the tasks (default is 8)
 */
abstract class BaseMsgService<T>(
    scope: CoroutineScope,
    private val numWorkers: Int = DEFAULT_NUMBER_WORKERS
) : CoroutineScope by scope {
    protected val captures = Channel<MessageCapture<T>>(CAPACITY_CAPTURE_BUFFER)
    protected val visibilityCaptures = Channel<MessageCapture<T>>(CAPACITY_VISIBILITY_CAPTURE_BUFFER)
    protected val respondResultCaptures = Channel<MessageCapture<T>>(CAPACITY_RESPONSE_CAPTURE_BUFFER)


    /**
     * Starts the service in a new launcher coroutine
     */
    fun start() = launch {
        initService()
        val requests = collectRequests()
        immortalWorkers(numWorkers, exceptionHandler = Companion::collectCaughtExceptions) { workerId ->
            for (request in requests) {
                val result = runCatching {
                    if (shouldProcessRequest(request)) {
                        logger.info { "Processing request - $request" }
                        processRequest(request)
                    } else {
                        logger.info { "SKIP request - $request" }
                        null
                    }
                }.onFailure { exc ->
                    logger.error(
                        "Worker '$workerId' on '${currentThread().name}' caught exception trying to process message '${request.jobId}'",
                        exc
                    )
                    collectCaughtExceptions(exc)
                }.getOrElse {
                    val error = when {
                        it is BaseJobError -> it
                        it.message == "request.message.body() must not be null" -> InvalidRequestError(
                            it,
                            "message.body() must not be null"
                        )
                        else -> LibraryInternalError(
                            it,
                            "Worker '$workerId' on '${currentThread().name}' failed processing message '${request.jobId}'\n${it.localizedMessage}"
                        )
                    }
                    JobResult.error(
                        request.taskId,
                        request.jobId,
                        error
                    )
                }
                if (result != null) {
                    captures.send(
                        if (result.isError) ErrorResultCapture(request, result)
                        else SuccessResultCapture(request, result)
                    )
                    respondResultCaptures.send(RespondResultCapture(request, result))
                }
            }
        }
        handleCaptures()
        handleVisibilityCaptures()
        handleRespondResultCaptures()
    }

    /**
     * Stops the service by cancelling all child coroutines
     */
    fun stop() = cancel()

    /**
     * Should process the given request message otherwise skip if returned false
     */
    protected open fun shouldProcessRequest(request: RequestMsg<T>): Boolean = true

    /**
     * Process the request message and returns the executed job result
     */
    abstract suspend fun processRequest(request: RequestMsg<T>): JobResult<*>

    protected abstract suspend fun collectRequests(): ReceiveChannel<RequestMsg<T>>

    protected abstract suspend fun handleCaptures(): Job
    protected abstract suspend fun handleVisibilityCaptures(): Job

    protected abstract suspend fun handleRespondResultCaptures(): Job

    /**
     * Initializes the service.
     *
     * Needs to be implemented by concrete implementations
     */
    protected abstract suspend fun initService()

    protected fun parseResult(result: JobResult<*>) = jobController.parser.serializeJobResult(result)

    companion object {
        val jobController: BaseJobController = JobController

        const val DEFAULT_NUMBER_WORKERS = 8
        const val DEFAULT_NUMBER_CAPTURE_WORKERS = 8
        const val DEFAULT_NUMBER_VISIBILITY_CAPTURE_WORKERS = 8
        const val DEFAULT_NUMBER_RESPONSE_MESSAGE_WORKERS = 8
        const val CAPACITY_CAPTURE_BUFFER = 80
        const val CAPACITY_VISIBILITY_CAPTURE_BUFFER = 80
        const val CAPACITY_RESPONSE_CAPTURE_BUFFER= 80
        const val CAPACITY_REQUEST_BUFFER = 40

        // Dummy method, mostly to verify exceptions in unit tests
        fun collectCaughtExceptions(exc: Throwable) {}
    }
}

sealed class MessageCapture<T>(val request: RequestMsg<T>)
class PingMessageCapture<T>(request: RequestMsg<T>, val attempt: Int = 1) : MessageCapture<T>(request)
class SuccessResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class ErrorResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class RespondResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
