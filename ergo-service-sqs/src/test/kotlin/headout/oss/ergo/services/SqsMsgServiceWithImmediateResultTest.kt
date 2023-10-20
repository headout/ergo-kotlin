package headout.oss.ergo.services

import com.google.common.truth.Truth.assertThat
import headout.oss.ergo.helpers.ImmediateRespondJobResultHandler
import headout.oss.ergo.models.JobResult
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.junit.Test
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest

/**
 * Created by shivanshs9 on 22/09/20.
 */
@ExperimentalCoroutinesApi
class SqsMsgServiceWithImmediateResultTest : BaseSqsServiceTest() {
    override fun createSqsMsgService(): SqsMsgService = SqsMsgService(
        sqsClient,
        QUEUE_URL,
        defaultVisibilityTimeout = VISIBILITY_TIMEOUT,
        resultHandler = ImmediateRespondJobResultHandler(),
        scope = testScope
    )

    @Test
    fun sufficientWorkersLaunchedOnStart() {
        msgService.start().apply {
            val workersCount = BaseMsgService.DEFAULT_NUMBER_WORKERS
            assertThat(children.count()).isEqualTo(workersCount)
        }
        val countLauncher = 1
        val countChannels = 2
        val countWorkerSupervisor = 1
        print(msgService.coroutineContext[Job]?.children?.count() ?: 0)
        assertThat(
            msgService.coroutineContext[Job]?.children?.count() ?: 0
        ).isEqualTo(countChannels + countLauncher + countWorkerSupervisor)
    }

    @Test
    fun whenWorkerEncounteredError_PushToResultQueue() {
        val msgCount = 2
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            (1..msgCount).forEach { _ ->
                delay(DELAY_WAIT)
                msgService["pushResults"](match<List<JobResult<*>>> { it.size == 1 })
            }
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfully_PushToResultQueue() {
        val msgCount = 2
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            (1..msgCount).forEach { _ ->
                delay(DELAY_WAIT)
                msgService["pushResults"](match<List<JobResult<*>>> { it.size == 1 })
            }
        }
    }

    @Test
    fun whenPushResultsCalled_BatchSendMessageToSqsQueue() {
        val msgCount = 2
        val taskId = "noArgWithSerializableResult"
        val body = ""
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            (1..msgCount).forEach { _ ->
                delay(DELAY_WAIT)
                msgService["pushResults"](match<List<JobResult<*>>> { it.size == 1 })
            }
        }
        verify {
            (1..msgCount).forEach { _ ->
                sqsClient.sendMessageBatch(match<SendMessageBatchRequest> {
                    val entries = it.entries()
                    println(entries)
                    it.queueUrl() == QUEUE_URL && entries.size == 1
                })
            }
        }
    }
}