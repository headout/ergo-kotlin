package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import kotlinx.metadata.KmClass
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * This cached API over [ClassInspector] that caches certain lookups Moshi does potentially multiple
 * times. This is useful mostly because it avoids duplicate reloads in cases like common base
 * classes, common enclosing types, etc.
 */
@KotlinPoetMetadataPreview
class CachedClassInspector(private val classInspector: ClassInspector){
    private val classToSpecCache = mutableMapOf<KClass<*>, TypeSpec>()

//    fun toImmutableKmClass(metadata: Metadata): KmClass =

//    fun toImmutableKmClass(element: TypeElement): ImmutableKmClass? = element.kotlinMetadata?.let {
//        toImmutableKmClass(it)
//    }
//
//    fun toTypeSpec(kmClass: ImmutableKmClass): TypeSpec = kmClassToSpecCache.getOrPut(kmClass) {
//        kmClass.toTypeSpec(classInspector)
//    }

    fun toTypeSpec(clazz: KClass<*>): TypeSpec = classToSpecCache.getOrPut(clazz) {
        clazz.toTypeSpec(true, classInspector)
    }
}
