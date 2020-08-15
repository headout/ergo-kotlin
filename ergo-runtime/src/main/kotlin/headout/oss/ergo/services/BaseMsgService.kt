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

abstract class BaseMsgService<T>(
    scope: CoroutineScope,
    private val numWorkers: Int = DEFAULT_NUMBER_WORKERS
) : CoroutineScope by scope {
    protected val captures = Channel<MessageCapture<T>>(CAPACITY_CAPTURE_BUFFER)

    fun start() = launch {
        initService()
        val requests = collectRequests()
        immortalWorkers(numWorkers, exceptionHandler = Companion::collectCaughtExceptions) { workerId ->
            for (request in requests) {
                val result = runCatching {
                    logger.info { "Processing request - $request" }
                    processRequest(request)
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
                captures.send(
                    if (result.isError) ErrorResultCapture(request, result)
                    else SuccessResultCapture(request, result)
                )
                captures.send(RespondResultCapture(request, result))
            }
        }
        handleCaptures()
    }

    fun stop() = cancel()

    abstract suspend fun processRequest(request: RequestMsg<T>): JobResult<*>

    protected abstract suspend fun collectRequests(): ReceiveChannel<RequestMsg<T>>

    protected abstract suspend fun handleCaptures(): Job

    protected abstract suspend fun initService()

    protected fun parseResult(result: JobResult<*>) = jobController.parser.serializeJobResult(result)

    companion object {
        val jobController: BaseJobController = JobController

        const val DEFAULT_NUMBER_WORKERS = 8
        const val CAPACITY_CAPTURE_BUFFER = 40
        const val CAPACITY_REQUEST_BUFFER = 20

        // Dummy method, mostly to verify exceptions in unit tests
        fun collectCaughtExceptions(exc: Throwable) {}
    }
}

sealed class MessageCapture<T>(val request: RequestMsg<T>)
class PingMessageCapture<T>(request: RequestMsg<T>, val attempt: Int = 1) : MessageCapture<T>(request)
class SuccessResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class ErrorResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class RespondResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
