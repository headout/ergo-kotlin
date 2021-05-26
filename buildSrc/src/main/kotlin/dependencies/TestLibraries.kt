package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created by shivanshs9 on 01/06/20.
 */
const val kotlinVersion = "1.5.0"

object TestLibraries {
    private object Versions {
        const val mockK = "1.10.0"
        const val coroutinesTest = "1.5.0"
        const val jUnit = "4.13"
    }

    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.4.1"
    const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"
    const val truth = "com.google.truth:truth:1.0.1"
    const val jUnit = "junit:junit:${Versions.jUnit}"
    const val mockK = "io.mockk:mockk:${Versions.mockK}"
    const val kotlinxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"
    const val simpleLogger = "org.slf4j:slf4j-simple:1.7.29"
}

fun DependencyHandler.testImplementsCodeGen() {
    add("testImplementation", TestLibraries.compileTesting)
    add("testImplementation", Libraries.kotlinMetadata)
    add("testImplementation", TestLibraries.compilerEmbeddable)
}

fun DependencyHandler.testImplementsCommon() {
    add("testApi", TestLibraries.jUnit)
    add("testImplementation", TestLibraries.truth)
    add("testImplementation", TestLibraries.simpleLogger)
}

fun DependencyHandler.testImplementsMock() {
    add("testImplementation", TestLibraries.mockK)
}

fun DependencyHandler.testImplementsCoroutines() {
    add("testImplementation", TestLibraries.kotlinxCoroutines)
}