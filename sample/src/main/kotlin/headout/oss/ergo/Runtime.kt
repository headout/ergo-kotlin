package headout.oss.ergo

import headout.oss.ergo.factory.JsonFactory
import headout.oss.ergo.helpers.ImmediateRespondJobResultHandler
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.services.SqsMsgService
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.UnitSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.parse
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI
import java.util.*

/**
 * Created by shivanshs9 on 05/06/20.
 */
val TASKS = listOf("suspend_oneArg.1", "suspend_oneArg.2", "suspend_oneArg.3")
val TASK_BODIES = listOf(
    "{}", "{\"num\": 3}"
)
val LOCAL_REGION: Region = Region.of("default")
val LOCAL_ENDPOINT: URI = URI.create("http://localhost:9324")
const val QUEUE_URL = "http://localhost:9324/queue/fifo_req_calipso"
const val RESULT_QUEUE_URL = "http://localhost:9324/queue/fifo_res"

fun CoroutineScope.produceTasks() = launch {
    println("${Thread.currentThread().name} Producing tasks")
    val sqsClient = SqsClient.builder()
        .endpointOverride(LOCAL_ENDPOINT)
        .region(LOCAL_REGION)
        .build()

    var i = 0
    while (isActive) {
        val taskIndex = TASKS.indices.random()
        val sendMsg = SendMessageRequest.builder()
            .messageGroupId(TASKS[taskIndex])
            .messageBody(TASK_BODIES.random())
            .queueUrl(QUEUE_URL)
            .messageDeduplicationId(Calendar.getInstance().timeInMillis.toString())
            .build()
        val id = sqsClient.sendMessage(sendMsg).messageId()
        println("Message sent with id: $id")
        delay((1000L..5000L).random())
        i++
    }
}

fun main() = runBlocking {
//    val springApp = MySpringApplication()
//    springApp.main()
    val input = "{\"taskId\":\"aries.vendor.deleteInvalidBookables\",\"jobId\":\"88a78f20-038b-4d38-8415-c412c9f3fe44\",\"data\":false,\"metadata\":{\"status\":200,\"error\":null}}"
    val output = JsonFactory.json.parse(JobResult.serializer(Boolean.Companion.serializer()), input)
    println(output)
/*
    println("${Thread.currentThread().name} Starting program")
    val job = produceTasks()
    val sqsClient = SqsAsyncClient.builder()
        .endpointOverride(LOCAL_ENDPOINT)
        .region(LOCAL_REGION)
        .build()
    val service = SqsMsgService(
        sqsClient,
        QUEUE_URL,
        RESULT_QUEUE_URL,
        numWorkers = 20,
        resultHandler = ImmediateRespondJobResultHandler()
    )
    val cronJob = service.start()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            super.run()
            service.stop()
//            springApp.stop()
            job.cancel()
        }
    })
    cronJob.join()
    service.stop()
    */
}