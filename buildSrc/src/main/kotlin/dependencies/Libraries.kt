package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

/**
 * Created by shivanshs9 on 01/06/20.
 */
object Libraries {
    private object Versions {
        const val kotlinPoet = "1.17.0"
        const val coroutine = "1.8.0"
        const val aws2 = "2.13.26"
        const val spring = "5.1.2.RELEASE"
    }

    const val autoService = "com.google.auto.service:auto-service:1.0-rc7"
    const val kotlinMetadata = "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0"
    const val kotlinPoetCore = "com.squareup:kotlinpoet:${Versions.kotlinPoet}"
    const val kotlinPoetMetadata = "com.squareup:kotlinpoet-metadata:1.17.0"
    const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2"
    const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutine}"
    const val coroutineJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutine}"
    const val aws2Bom = "software.amazon.awssdk:bom:${Versions.aws2}"
    const val aws2Sqs = "software.amazon.awssdk:sqs"
    const val awsSts = "software.amazon.awssdk:sts:1.12.604"
    const val springCore = "org.springframework:spring-core:${Versions.spring}"
    const val springContext = "org.springframework:spring-context:${Versions.spring}"
    const val springBeans = "org.springframework:spring-beans:${Versions.spring}"
    const val logging = "io.github.microutils:kotlin-logging:1.7.10"
}

fun DependencyHandler.implementsKotlinPoet() {
    add("implementation", Libraries.kotlinMetadata)
    add("implementation", Libraries.kotlinPoetMetadata)
    add("implementation", Libraries.kotlinPoetCore)
    add("implementation", "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
}

fun DependencyHandler.implementsCodeGen() {
    add("implementation", Libraries.autoService)
    add("kapt", Libraries.autoService)
    implementsKotlinPoet()
}

fun DependencyHandler.implementsSerialization() {
    add("implementation", Libraries.serializationJson)
}

fun DependencyHandler.implementsCommon() {
    add("implementation", kotlin("stdlib-jdk8"))
    add("implementation", Libraries.logging)
}

fun DependencyHandler.implementsCoroutine() {
    add("implementation", Libraries.coroutine)
}

fun DependencyHandler.implementsAwsSqs() {
    add("implementation", platform(Libraries.aws2Bom))
    add("implementation", Libraries.aws2Sqs)
    add("implementation", Libraries.awsSts)
}

fun DependencyHandler.implementsReflection() {
    add("implementation", kotlin("reflect"))
}

fun DependencyHandler.implementsSpring() {
    add("implementation", Libraries.springCore)
    add("implementation", Libraries.springBeans)
    add("implementation", Libraries.springContext)
}