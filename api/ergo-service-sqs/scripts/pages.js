var pages = [{'name': 'class ImmediateRespondJobResultHandler : JobResultHandler', 'description':'headout.oss.ergo.helpers.ImmediateRespondJobResultHandler', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-immediate-respond-job-result-handler/index.html', 'searchKey':'ImmediateRespondJobResultHandler'},
{'name': 'fun ImmediateRespondJobResultHandler()', 'description':'headout.oss.ergo.helpers.ImmediateRespondJobResultHandler.ImmediateRespondJobResultHandler', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-immediate-respond-job-result-handler/-immediate-respond-job-result-handler.html', 'searchKey':'ImmediateRespondJobResultHandler'},
{'name': 'open suspend override fun handleResult(result: <ERROR CLASS><Out <ERROR CLASS>>): Boolean', 'description':'headout.oss.ergo.helpers.ImmediateRespondJobResultHandler.handleResult', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-immediate-respond-job-result-handler/handle-result.html', 'searchKey':'handleResult'},
{'name': 'open suspend override fun handleResult(result: <ERROR CLASS><Out <ERROR CLASS>>): Boolean', 'description':'headout.oss.ergo.helpers.InMemoryBufferJobResultHandler.handleResult', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-in-memory-buffer-job-result-handler/handle-result.html', 'searchKey':'handleResult'},
{'name': 'abstract suspend fun handleResult(result: <ERROR CLASS><Out <ERROR CLASS>>): Boolean', 'description':'headout.oss.ergo.helpers.JobResultHandler.handleResult', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-job-result-handler/handle-result.html', 'searchKey':'handleResult'},
{'name': 'open fun init(scope: <ERROR CLASS>, jobParser: <ERROR CLASS>, pushResultsImpl: SuspendFunction1<List<<ERROR CLASS><Out <ERROR CLASS>>>, Unit>)', 'description':'headout.oss.ergo.helpers.ImmediateRespondJobResultHandler.init', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-immediate-respond-job-result-handler/init.html', 'searchKey':'init'},
{'name': 'open fun init(scope: <ERROR CLASS>, jobParser: <ERROR CLASS>, pushResultsImpl: SuspendFunction1<List<<ERROR CLASS><Out <ERROR CLASS>>>, Unit>)', 'description':'headout.oss.ergo.helpers.InMemoryBufferJobResultHandler.init', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-in-memory-buffer-job-result-handler/init.html', 'searchKey':'init'},
{'name': 'abstract fun init(scope: <ERROR CLASS>, jobParser: <ERROR CLASS>, pushResultsImpl: SuspendFunction1<List<<ERROR CLASS><Out <ERROR CLASS>>>, Unit>)', 'description':'headout.oss.ergo.helpers.JobResultHandler.init', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-job-result-handler/init.html', 'searchKey':'init'},
{'name': 'class InMemoryBufferJobResultHandler(maxResultsToBuffer: Int, timeoutCollectResult: Long) : JobResultHandler', 'description':'headout.oss.ergo.helpers.InMemoryBufferJobResultHandler', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-in-memory-buffer-job-result-handler/index.html', 'searchKey':'InMemoryBufferJobResultHandler'},
{'name': 'fun InMemoryBufferJobResultHandler(maxResultsToBuffer: Int, timeoutCollectResult: Long)', 'description':'headout.oss.ergo.helpers.InMemoryBufferJobResultHandler.InMemoryBufferJobResultHandler', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-in-memory-buffer-job-result-handler/-in-memory-buffer-job-result-handler.html', 'searchKey':'InMemoryBufferJobResultHandler'},
{'name': 'object Companion', 'description':'headout.oss.ergo.helpers.InMemoryBufferJobResultHandler.Companion', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-in-memory-buffer-job-result-handler/-companion/index.html', 'searchKey':'Companion'},
{'name': 'object Companion', 'description':'headout.oss.ergo.services.SqsMsgService.Companion', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-companion/index.html', 'searchKey':'Companion'},
{'name': 'interface JobResultHandler', 'description':'headout.oss.ergo.helpers.JobResultHandler', 'location':'ergo-service-sqs/headout.oss.ergo.helpers/-job-result-handler/index.html', 'searchKey':'JobResultHandler'},
{'name': 'class SqsMsgService(sqs: <ERROR CLASS>, requestQueueUrl: String, resultQueueUrl: String, defaultVisibilityTimeout: Long?, numWorkers: Int, resultHandler: JobResultHandler, scope: <ERROR CLASS>)', 'description':'headout.oss.ergo.services.SqsMsgService', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/index.html', 'searchKey':'SqsMsgService'},
{'name': 'fun SqsMsgService(sqs: <ERROR CLASS>, requestQueueUrl: String, resultQueueUrl: String, defaultVisibilityTimeout: Long?, numWorkers: Int, resultHandler: JobResultHandler, scope: <ERROR CLASS>)', 'description':'headout.oss.ergo.services.SqsMsgService.SqsMsgService', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-sqs-msg-service.html', 'searchKey':'SqsMsgService'},
{'name': 'open fun fromTaskId(taskId: <ERROR CLASS>): String', 'description':'headout.oss.ergo.services.SqsMsgService.Companion.fromTaskId', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-companion/from-task-id.html', 'searchKey':'fromTaskId'},
{'name': 'fun getPingDelay(visibilityTimeout: Long): Long', 'description':'headout.oss.ergo.services.SqsMsgService.Companion.getPingDelay', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-companion/get-ping-delay.html', 'searchKey':'getPingDelay'},
{'name': 'fun getVisibilityTimeoutForAttempt(visibilityTimeout: Long, attempt: Int): <ERROR CLASS>', 'description':'headout.oss.ergo.services.SqsMsgService.Companion.getVisibilityTimeoutForAttempt', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-companion/get-visibility-timeout-for-attempt.html', 'searchKey':'getVisibilityTimeoutForAttempt'},
{'name': 'open fun toTaskId(value: String): <ERROR CLASS>', 'description':'headout.oss.ergo.services.SqsMsgService.Companion.toTaskId', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/-companion/to-task-id.html', 'searchKey':'toTaskId'},
{'name': 'open suspend fun collectRequests(): <ERROR CLASS><<ERROR CLASS><<ERROR CLASS>>>', 'description':'headout.oss.ergo.services.SqsMsgService.collectRequests', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/collect-requests.html', 'searchKey':'collectRequests'},
{'name': 'open suspend fun handleCaptures(): <ERROR CLASS>', 'description':'headout.oss.ergo.services.SqsMsgService.handleCaptures', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/handle-captures.html', 'searchKey':'handleCaptures'},
{'name': 'open suspend fun initService()', 'description':'headout.oss.ergo.services.SqsMsgService.initService', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/init-service.html', 'searchKey':'initService'},
{'name': 'open suspend fun processRequest(request: <ERROR CLASS><<ERROR CLASS>>): <ERROR CLASS><Out <ERROR CLASS>>', 'description':'headout.oss.ergo.services.SqsMsgService.processRequest', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/process-request.html', 'searchKey':'processRequest'},
{'name': 'open fun shouldProcessRequest(request: <ERROR CLASS><<ERROR CLASS>>): Boolean', 'description':'headout.oss.ergo.services.SqsMsgService.shouldProcessRequest', 'location':'ergo-service-sqs/headout.oss.ergo.services/-sqs-msg-service/should-process-request.html', 'searchKey':'shouldProcessRequest'},
{'name': 'suspend fun <ERROR CLASS>.getVisibilityTimeout(queueUrl: String): Long', 'description':'headout.oss.ergo.services.getVisibilityTimeout', 'location':'ergo-service-sqs/headout.oss.ergo.services/get-visibility-timeout.html', 'searchKey':'getVisibilityTimeout'}]
