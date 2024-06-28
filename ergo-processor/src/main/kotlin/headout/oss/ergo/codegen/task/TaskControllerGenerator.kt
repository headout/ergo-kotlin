package headout.oss.ergo.codegen.task

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.codegen.api.CachedClassInspector
import headout.oss.ergo.codegen.api.TargetType
import headout.oss.ergo.codegen.api.TypeGenerator
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.factory.BaseTaskController
import headout.oss.ergo.factory.InstanceLocatorFactory
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.utils.*
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskControllerGenerator internal constructor(
    private val enclosingElement: TypeElement,
    private val targetType: TargetType,
    val bindingClassName: ClassName,
    val tasks: List<TaskMethodGenerator>,
    private val callTaskSpecBuilder: FunSpec.Builder
) : TypeGenerator {

    private val classTypeVariables by lazy {
        arrayOf(
            TypeVariableName.invoke(TYPE_ARG_REQUEST, JobRequestData::class.asTypeName()),
            TypeVariableName.invoke(TYPE_ARG_RESULT)
        )
    }

    override fun brewKotlin(brewHook: (FileSpec.Builder) -> Unit): FileSpec = createType().let { bindingType ->
        FileSpec.builder(bindingClassName.packageName, bindingType.name!!)
            .addType(bindingType)
            .apply {
                brewHook(this)
                tasks.forEach {
                    if (it.isRequestDataNeeded()) addType(it.createRequestDataSpec())
                }
            }
            .addFileComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()
    }

    private fun createType(): TypeSpec = TypeSpec.classBuilder(bindingClassName.simpleName)
        .addModifiers(KModifier.PUBLIC)
        .addOriginatingElement(enclosingElement)
        .apply {
            val instanceProp = PropertySpec.builder(PROP_INSTANCE, targetType.className, KModifier.PRIVATE)
                .initializer(
                    "%M(%L)",
                    InstanceLocatorFactory::class.member("getInstance"),
                    "${enclosingElement.simpleName}::class"
                )
                .build()
            addProperty(instanceProp)
        }
        .addTypeVariables(*classTypeVariables)
        .superclass(BaseTaskController::class, *classTypeVariables)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec(PARAM_TASKID, TaskId::class.asTypeName()))
                .addParameter(ParameterSpec(PARAM_JOBID, JobId::class.asTypeName()))
                .addParameter(ParameterSpec(PARAM_REQUESTDATA, classTypeVariables[0]))
                .build()
        )
        .addSuperclassConstructorParameters(
            PARAM_TASKID,
            PARAM_JOBID,
            PARAM_REQUESTDATA
        )
        .apply {
            addFunctions(createFunctions())
        }
        .addFunction(overrideCallTaskMethod())
        .build()

    private fun createFunctions() = tasks.map {
        it.createFunctionSpec { funBuilder ->
            funBuilder.returns(classTypeVariables[1])
        }
    }

    private fun overrideCallTaskMethod(): FunSpec = callTaskSpecBuilder
        .addModifiers(KModifier.OVERRIDE)
        .beginControlFlow("return when (taskId)")
        .apply {
            tasks.forEach {
                addStatement(
                    "%S -> %N(%N as %T)",
                    it.task.taskId,
                    it.methodName,
                    "jobRequest",
                    it.getRequestType(it.requestDataClassName)
                )
            }
            addStatement("else -> %M($PARAM_TASKID)", ExceptionUtils::class.member("taskNotFound"))
        }
        .endControlFlow()
        .build()

    @KotlinPoetMetadataPreview
    class Builder internal constructor(
        private val targetType: TargetType,
        private val typeElement: TypeElement,
        private val classInspector: CachedClassInspector
    ) {
        private val taskBuilders: MutableSet<TaskMethodGenerator.Builder> = mutableSetOf()

        fun addMethod(task: Task, methodName: String): Boolean {
            val method = targetType.methods[methodName] ?: return false
            TaskMethodGenerator.builder(
                task,
                method,
                targetType.className
            ).also { taskBuilders.add(it) }
            return true
        }

        fun build(): TaskControllerGenerator {
            val tasks = taskBuilders.map { it.build() }
            val callTaskBuilder = classInspector.toTypeSpec(BaseTaskController::class).overrideFunction("callTask")
            return TaskControllerGenerator(
                typeElement,
                targetType,
                typeElement.getBindingClassName(),
                tasks,
                callTaskBuilder
            )
        }
    }

    companion object {
        const val TYPE_ARG_REQUEST = "Req"
        const val TYPE_ARG_RESULT = "Res"

        const val PARAM_TASKID = "taskId"
        const val PARAM_JOBID = "jobId"
        const val PARAM_REQUESTDATA = "requestData"

        const val ARG_JOB_REQUEST = "jobRequest"
        const val ARG_JOB_CALLBACK = "jobCallback"

        const val PROP_INSTANCE = "instance"

        @KotlinPoetMetadataPreview
        fun builder(
            targetType: TargetType,
            typeElement: TypeElement,
            classInspector: CachedClassInspector
        ): Builder {
            return Builder(
                targetType,
                typeElement,
                classInspector
            )
        }
    }
}

private fun TypeElement.getBindingClassName(): ClassName {
    val packageName = packageElement.qualifiedName.toString()
    val className = qualifiedName.toString().substring(packageName.length + 1)
        .replace('.', '$') // since .simpleName is unreliable and sometimes blank
    return ClassName(packageName, "${className}_TaskBinding")
}
