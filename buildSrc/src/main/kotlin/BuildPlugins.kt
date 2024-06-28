import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.gradle.plugin.use.PluginDependenciesSpec

/**
 * Created by shivanshs9 on 01/06/20.
 */
// These properties and functions cannot be moved to any other package due to this bug -
// https://github.com/gradle/gradle/issues/9270 (targeted at V7.0)
const val kotlinVersion = "1.9.24"

fun PluginDependenciesSpec.kotlinJvm() {
    kotlin("jvm") version kotlinVersion
}

fun PluginDependenciesSpec.kotlinKapt() {
    kotlin("kapt") version kotlinVersion
}

fun PluginDependenciesSpec.kotlinxSerialization() {
    kotlin("plugin.serialization") version kotlinVersion
}

fun PluginDependenciesSpec.kotlinDoc() {
    id("org.jetbrains.dokka")
}