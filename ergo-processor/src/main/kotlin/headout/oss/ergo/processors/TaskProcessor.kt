package headout.oss.ergo.processors

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.codegen.api.CachedClassInspector
import headout.oss.ergo.codegen.targetType
import headout.oss.ergo.codegen.task.TaskControllerGenerator
import headout.oss.ergo.factory.IJobParser
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.IOException
import javax.annotation.processing.FilerException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * Created by shivanshs9 on 20/05/20.
 */
@OptIn(KotlinPoetMetadataPreview::class)
@AutoService(Processor::class)
class TaskProcessor : KotlinAbstractProcessor() {
    internal val classInspector by lazy {
        CachedClassInspector(
            ElementsClassInspector.create(true, elementUtils, typeUtils)
        )
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(ANNOTATION_TYPE.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val bindingMap = processTargets(roundEnv)
        bindingMap.forEach { binding ->
            binding.value.runCatching {
                brewKotlin().writeTo(filer)
            }.onFailure { handleBrewError(it, binding.key) }
        }
        kotlin.runCatching {
            val jobParserApi = classInspector.toTypeSpec(IJobParser::class)
            val jobParserBinder = JobParserBinder(bindingMap, jobParserApi, this)
            jobParserBinder.brewKotlin().writeTo(filer)
        }.onFailure { if (it !is FilerException) error(it) }
        return false
    }

    private fun processTargets(roundEnv: RoundEnvironment): Map<TypeElement, TaskControllerGenerator> {
        val builderMap = mutableMapOf<TypeElement, TaskControllerGenerator.Builder>()
        roundEnv.getElementsAnnotatedWith(ANNOTATION_TYPE).forEach { element ->
            if (element.kind == ElementKind.METHOD && element is ExecutableElement) {
                processMethodBinding(roundEnv, element, builderMap)
            } else messager.printMessage(Diagnostic.Kind.ERROR, "Cannot annotate anything but a method")
        }
        return builderMap.mapValues { it.value.build() }
    }

    private fun processMethodBinding(
        roundEnv: RoundEnvironment,
        element: ExecutableElement,
        builderMap: MutableMap<TypeElement, TaskControllerGenerator.Builder>
    ) {
        val classElement = (element.enclosingElement as TypeElement)
        val methodName = element.simpleName.toString()
        val annotation = element.getAnnotation(ANNOTATION_TYPE)
        builderMap.attachElement(classElement, this).apply {
            addMethod(annotation, methodName)
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    private fun handleBrewError(error: Throwable, element: Element) {
        val msg = when (error) {
            is IOException -> "Unable to write binding for type %s: %s"
            else -> "Encountered unknown error processing %s: %s"
        }
        error(
            error,
            element,
            msg,
            element.toString(),
            error.localizedMessage ?: error.message ?: error.toString()
        )
    }

    private fun error(exc: Throwable, element: Element, message: String? = null, vararg args: Any) {
        exc.printStackTrace()
        printMessage(Diagnostic.Kind.ERROR, element, message ?: exc.localizedMessage ?: exc.toString(), args)
    }

    private fun error(exc: Throwable, message: String? = null, vararg args: Any) {
        exc.printStackTrace()
        printMessage(Diagnostic.Kind.ERROR, null, message ?: exc.localizedMessage ?: exc.toString(), args)
    }

    private fun printMessage(
        kind: Diagnostic.Kind,
        element: Element? = null,
        message: String,
        vararg args: Any
    ) {
        val msg = if (args.isNotEmpty()) message.format(args) else message
        if (element != null) messager.printMessage(kind, msg, element)
        else messager.printMessage(kind, msg)
    }

    companion object {
        val ANNOTATION_TYPE = Task::class.java
    }
}

@KotlinPoetMetadataPreview
private fun MutableMap<TypeElement, TaskControllerGenerator.Builder>.attachElement(
    enclosingElement: TypeElement,
    taskProcessor: TaskProcessor
) =
    getOrPut(enclosingElement) {
        val targetType = taskProcessor.targetType(enclosingElement, taskProcessor.classInspector)
        TaskControllerGenerator.builder(targetType, enclosingElement, taskProcessor.classInspector)
    }