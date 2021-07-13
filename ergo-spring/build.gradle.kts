import dependencies.*
import publish.GithubPackage

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinDoc()
}

kapt {
    generateStubs = true
}

dependencies {
    implementsCommon()
    implementsReflection()
    implementsSerialization()
    implementsCoroutine()
    implementation(Libraries.coroutineJdk8)
    implementation(project(":ergo-runtime"))
    implementsSpring()

    kaptTest(project(":ergo-processor"))
    testImplementsCommon()
    testImplementsCoroutines()
    testImplementsMock()
}

publishing {
    GithubPackage(project)
}

apply(from = rootProject.file("gradle/common.gradle.kts"))
