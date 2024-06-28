package headout.oss.ergo.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import headout.oss.ergo.codegen.api.CachedClassInspector
import headout.oss.ergo.codegen.api.TargetMethod
import headout.oss.ergo.codegen.api.TargetParameter
import headout.oss.ergo.codegen.api.TargetType
import headout.oss.ergo.utils.kotlinMetadata
import headout.oss.ergo.utils.toFunSpec
import headout.oss.ergo.utils.visibility
import kotlinx.metadata.internal.extensions.KmClassExtension
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 13/08/20.
 */
private fun KotlinProcessingEnvironment.javaTargetType(
    element: TypeElement
): TargetType {
    val methods = elementUtils.getAllMembers(element)
        .filter { it.kind == ElementKind.METHOD }
        .map { elem ->
            val method = elem as ExecutableElement
            val funSpec = method.toFunSpec().apply {
                modifiers.remove(KModifier.OVERRIDE)
            }
            TargetMethod(
                name = method.simpleName.toString(),
                returnType = method.returnType.asTypeName(),
                modifiers = funSpec.modifiers,
                parameters = funSpec.parameters.toTargetParameters(),
                isStatic = method.modifiers.contains(Modifier.STATIC)
            )
        }.associateBy { it.name }
    return TargetType(
        className = element.asClassName(),
        methods = methods,
        visibility = element.modifiers.visibility()
    )
}

@KotlinPoetMetadataPreview
internal fun KotlinProcessingEnvironment.targetType(
    element: TypeElement,
    classInspector: CachedClassInspector
): TargetType {
    return element.getAnnotation(Metadata::class.java)?.let {
        val api = element.toTypeSpec(true)
        val methods = api.funSpecs.map {
            TargetMethod(
                name = it.name,
                returnType = it.returnType,
                modifiers = it.modifiers,
                parameters = it.parameters.toTargetParameters()
            )
        }.associateBy { it.name }
        return TargetType(
            className = element.asClassName(),
            methods = methods,
            visibility = api.modifiers.visibility()
        )
    } ?: return javaTargetType(element)
}

private fun Collection<ParameterSpec>.toTargetParameters() = mapIndexed { index, parameterSpec ->
    TargetParameter(
        name = parameterSpec.name,
        index = index,
        type = parameterSpec.type,
        defaultValue = parameterSpec.defaultValue
    )
}
