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
