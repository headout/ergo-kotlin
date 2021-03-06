package headout.oss.ergo.factory

import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */

/**
 * Supports instances of singleton classes and classes with a no-arg constructor
 */
class DefaultInstanceLocator : InstanceLocator {
    override fun <T : Any> getInstance(clazz: KClass<T>): T? {
        return clazz.objectInstance ?: clazz.constructors.let {
            it.find { it.parameters.isEmpty() }?.call()
        }
    }
}