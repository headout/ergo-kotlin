package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import headout.oss.ergo.codegen.api.TypeGenerator
import headout.oss.ergo.codegen.task.TaskControllerGenerator
import headout.oss.ergo.codegen.task.TaskMethodGenerator
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.factory.IJobParser
import headout.oss.ergo.factory.JsonFactory
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.utils.overrideFunction
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 30/05/20.
 */
class JobParserBinder(
    private val bindingMap: Map<TypeElement, TaskControllerGenerator>,
    private val jobParserApi: TypeSpec,
    processingEnvironment: KotlinProcessingEnvironment
) : TypeGenerator, KotlinProcessingEnvironment by processingEnvironment {

    override fun brewKotlin(brewHook: (FileSpec.Builder) -> Unit): FileSpec =
        FileSpec.builder(IJobParser::class.java.packageName, IJobParser.CLASS_NAME_JOB_PARSER)
            .addType(createJobRequestParser())
            .addComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()

    private fun createJobRequestParser() = TypeSpec.objectBuilder(
        IJobParser.CLASS_NAME_JOB_PARSER
    )
        .addSuperinterface(IJobParser::class)
        .addFunction(createParseRequestDataFunction())
        .addFunction(createNewTaskControllerFunction())
        .addFunction(createSerializeJobResultFunction())
        .addFunction(createDeserializeJobResultFunction())
        .build()

    private fun createParseRequestDataFunction() = jobParserApi.overrideFunction("parseRequestData")
        .beginControlFlow("return when (%N)", "taskId")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val emptyRequestClassName = EmptyRequestData::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    if (taskBind.isRequestDataNeeded()) addStatement(
                        "%S -> %M.decodeFromString(%T.serializer(), %N)",
                        taskBind.task.taskId,
                        jsonRef,
                        taskBind.requestDataClassName,
                        "rawData"
                    )
                    else addStatement("%S -> %T", taskBind.task.taskId, emptyRequestClassName)
                }
            }
            addStatement("else -> %T", emptyRequestClassName)
        }
        .endControlFlow()
        .build()

    private fun createNewTaskControllerFunction() = jobParserApi.overrideFunction("newTaskController")
        .beginControlFlow("return when (%N)", "taskId")
        .apply {
            bindingMap.values.forEach { controllerBind ->
                val controllerClass = controllerBind.bindingClassName
                controllerBind.tasks.forEach { taskBind ->
                    val requestDataClass = taskBind.requestDataClassName
                    val resultClass = taskBind.targetMethod.returnType
                    val paramControllerClass = controllerClass.parameterizedBy(requestDataClass, resultClass)
                    addStatement(
                        "%S -> %T(%N, %N, %N as %T)",
                        taskBind.task.taskId,
                        paramControllerClass,
                        "taskId",
                        "jobId",
                        "requestData",
                        requestDataClass
                    )
                }
            }
            addStatement("else -> %M(%N)", MemberName(ExceptionUtils::class.asClassName(), "taskNotFound"), "taskId")
        }
        .endControlFlow()
        .build()

    private fun createSerializeJobResultFunction() = jobParserApi.overrideFunction("serializeJobResult")
        .beginControlFlow("return when (%N.taskId)", "jobResult")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val jobResultClass = JobResult::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    val paramJobResultClass = addSerializerForTask(taskBind, jobResultClass)
                    addStatement("%M.encodeToString(serializer, %N as %T)", jsonRef, "jobResult", paramJobResultClass)
                    endControlFlow()
                }
            }
            beginControlFlow("else ->")
            addStatement(
                "val serializer = %T.serializer(%M())",
                jobResultClass,
                BUILTIN_SERIALIZERS.getValue(Unit::class.asClassName())
            )
            addStatement("%M.decodeFromString(serializer, %N as JobResult<Unit>)", jsonRef, "jobResult")
            endControlFlow()
        }
        .endControlFlow()
        .build()

    private fun createDeserializeJobResultFunction() = jobParserApi.overrideFunction("deserializeJobResult")
        .beginControlFlow("return when (%N)", "taskId")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val jobResultClass = JobResult::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    addSerializerForTask(taskBind, jobResultClass)
                    addStatement("%M.decodeFromString(%N, %N)", jsonRef, "serializer", "rawData")
                    endControlFlow()
                }
            }
            beginControlFlow("else ->")
            addStatement(
                "val serializer = %T.serializer(%M())",
                jobResultClass,
                BUILTIN_SERIALIZERS.getValue(Unit::class.asClassName())
            )
            addStatement("%M.decodeFromString(%N, %N)", jsonRef, "serializer", "rawData")
            endControlFlow()
        }
        .endControlFlow()
        .build()

    private fun FunSpec.Builder.addSerializerForTask(
        taskBind: TaskMethodGenerator,
        jobResultClass: ClassName
    ): ParameterizedTypeName {
        val resultClass = taskBind.targetMethod.returnType
        val resultSerializer = BUILTIN_SERIALIZERS[resultClass]
        val paramJobResultClass = jobResultClass.parameterizedBy(resultClass)
        beginControlFlow("%S ->", taskBind.task.taskId)
        if (resultSerializer != null) {
            if (resultSerializer == companionSerializerFunction) addStatement(
                "val serializer = %T.serializer(%T.%M())",
                jobResultClass,
                resultClass,
                resultSerializer
            )
            else addStatement(
                "val serializer = %T.serializer(%M())",
                jobResultClass,
                resultSerializer
            )
        } else {
//            require(resultClass.annotations.find { it.className == serializableClassName } != null) {
//                "Return type $resultClass of ${taskBind.method.name} must be annotated with $serializableClassName"
//            }
            addStatement(
                "val serializer = %T.serializer(%T.serializer())",
                jobResultClass,
                resultClass
            )
        }
        return paramJobResultClass
    }

    companion object {
        private val companionSerializerFunction = MemberName("kotlinx.serialization.builtins", "serializer")

        private val BUILTIN_SERIALIZERS = mapOf(
            String::class to "serializer",
            Char::class to "serializer",
            CharArray::class to "CharArraySerializer",
            Double::class to "serializer",
            DoubleArray::class to "DoubleArraySerializer",
            Float::class to "serializer",
            FloatArray::class to "FloatArraySerializer",
            Long::class to "serializer",
            LongArray::class to "LongArraySerializer",
            Int::class to "serializer",
            IntArray::class to "IntArraySerializer",
            Short::class to "serializer",
            ShortArray::class to "ShortArraySerializer",
            Byte::class to "serializer",
            ByteArray::class to "ByteArraySerializer",
            Boolean::class to "serializer",
            BooleanArray::class to "BooleanArraySerializer",
            Unit::class to "serializer"
        )
            .mapKeys { it.key.asClassName() }
            .mapValues { MemberName("kotlinx.serialization.builtins", it.value) }
    }
}