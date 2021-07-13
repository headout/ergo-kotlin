package headout.oss.ergo.services

import headout.oss.ergo.BaseTest
import headout.oss.ergo.helpers.InMemoryBufferJobResultHandler
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Created by shivanshs9 on 22/09/20.
 */
@ExperimentalCoroutinesApi
abstract class BaseSqsServiceTest : BaseTest() {
    protected val sqsClient: SqsAsyncClient = mockk(relaxed = true)
    protected lateinit var msgService: SqsMsgService

    override fun beforeTest() {
        super.beforeTest()
        msgService = spyk(createSqsMsgService(), recordPrivateCalls = true)
        mockkObject(BaseMsgService.Companion)
        mockCommonSqsClient()
    }

    override fun afterTest() {
        super.afterTest()
        msgService.stop()
    }

    protected abstract fun createSqsMsgService(): SqsMsgService

    private fun mockCommonSqsClient() {
        val deleteSlot = slot<Consumer<DeleteMessageRequest.Builder>>()
        every { sqsClient.deleteMessage(capture(deleteSlot)) } answers {
            callOriginal()
        }
    }

    protected fun mockReceiveMessageResponse(
        jobId: String = "jobId",
        taskId: String? = null,
        body: String? = null,
        receiptHandle: String = "receipt",
        msgCount: Int = 1
    ) {
        val response = ReceiveMessageResponse.builder()
            .messages(
                List(msgCount) {
                    Message.builder()
                        .messageId("$jobId-$it")
                        .apply {
                            taskId?.let {
                                attributes(mapOf(MessageSystemAttributeName.MESSAGE_GROUP_ID to it))
                            }
                        }
                        .apply {
                            body?.also { body(it) }
                        }
                        .receiptHandle(receiptHandle)
                        .build()
                }
            )
            .build()
        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returnsMany listOf<CompletableFuture<ReceiveMessageResponse>>(
            CompletableFuture.completedFuture(response),
            CompletableFuture.supplyAsync {
                Thread.sleep(InMemoryBufferJobResultHandler.TIMEOUT_RESULT_COLLECTION * 2) // to ensure the error happens only after results are pushed
                error("Dummy error to mark failure on receiving messages")
            }
        )
    }

    companion object {
        const val QUEUE_URL = "sqs://queue"
        const val DELAY_WAIT = 1000L

        val VISIBILITY_TIMEOUT: Long = TimeUnit.SECONDS.toSeconds(5)
    }
}