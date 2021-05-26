package headout.oss.ergo.exceptions

import headout.oss.ergo.annotations.TaskId
import kotlinx.coroutines.CancellationException
//import kotlinx.io.ByteArrayOutputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

/**
 * Created by shivanshs9 on 24/05/20.
 */
object ExceptionUtils {
    // Called by client on error
    fun processClientError(error: Throwable): BaseJobError = when (error) {
        is CancellationException -> JobCancellationError(error)
        is BaseClientError -> error
        else -> ClientInternalError(error)
    }

    // Called by library on error
    fun processLibraryError(error: Throwable): BaseLibraryError = when (error) {
        is CancellationException -> JobCancellationError(error)
        is BaseLibraryError -> error
        else -> LibraryInternalError(error)
    }

    fun <T : Any> handleAndWrapError(errorHandler: (Throwable) -> Throwable, block: () -> T): T {
        return try {
            block()
        } catch (exc: Throwable) {
            throw errorHandler(exc)
        }
    }

    fun taskNotFound(taskId: TaskId): Nothing =
        throw TaskNotFoundError(msg = "Could not find relevant function for the taskId - '$taskId'")

    fun Throwable.getStackTraceWithCause(): String {
        val utf8Enc = StandardCharsets.UTF_8
        return ByteArrayOutputStream().use { byteArrStream ->
            val printStream = PrintStream(byteArrStream, true, utf8Enc)
            printStackTrace(printStream)
            byteArrStream.toString(utf8Enc)
        }
    }
}