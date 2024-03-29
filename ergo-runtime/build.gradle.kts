import dependencies.* // ktlint-disable no-wildcard-imports
import java.io.File
import java.io.FileInputStream
import java.util.*

val props = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}

plugins {
    kotlinJvm()
    kotlinDoc()
    kotlinxSerialization()
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementsReflection()
    api(project(":ergo-annotations"))

    testImplementation(project(":ergo-processor"))
    testImplementsCommon()
    testImplementsCodeGen()
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
