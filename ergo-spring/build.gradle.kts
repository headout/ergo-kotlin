import dependencies.* // ktlint-disable no-wildcard-imports
import java.io.File
import java.io.FileInputStream
import java.util.*

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinDoc()
}

val props = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
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
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "HeadoutHostedRepository"
            url = uri("http://nexus.headout.com/repository/maven-headout-internal/")
            credentials {
                username = System.getenv("HEADOUT_REPOSITORY_USERNAME") ?: props.getProperty("headoutRepositoryUsername")
                password = System.getenv("HEADOUT_REPOSITORY_PASSWORD") ?: props.getProperty("headoutRepositoryPassword")
            }
            isAllowInsecureProtocol = true
        }
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))
