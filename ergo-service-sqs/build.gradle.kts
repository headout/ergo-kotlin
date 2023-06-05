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
