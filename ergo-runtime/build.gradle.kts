import dependencies.*
import publish.GithubPackage

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
                username = System.getenv("HEADOUT_REPOSITORY_USERNAME")
                password = System.getenv("HEADOUT_REPOSITORY_PASSWORD")
            }
            isAllowInsecureProtocol = true
        }
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))