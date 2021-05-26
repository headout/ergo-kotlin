import dependencies.*
import publish.GithubPackage

plugins {
    kotlinJvm()
    kotlinxSerialization()
    kotlinKapt()
    kotlinDoc()
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCodeGen()
    implementation(project(":ergo-annotations"))
    implementation(project(":ergo-runtime"))

    testImplementsCommon()
    testImplementsCodeGen()

    // Provides serialization compiler plugin for compiler-testing
    testImplementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
}

publishing {
    GithubPackage(project)
}

apply(from = rootProject.file("gradle/common.gradle.kts"))