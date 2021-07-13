import dependencies.*
import publish.GithubPackage

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinDoc()
    kotlinxSerialization()
}

kapt {
    generateStubs = true
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementation(Libraries.coroutineJdk8)
    implementsAwsSqs()
    api(project(":ergo-runtime"))
    kaptTest(project(":ergo-processor"))

    testImplementsCommon()
    testImplementsCoroutines()
    testImplementsMock()
}

publishing {
    GithubPackage(project)
}

apply(from = rootProject.file("gradle/common.gradle.kts"))
