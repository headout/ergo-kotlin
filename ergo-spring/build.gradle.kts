import dependencies.*

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
            name = "ArtifactoryPackages"
            url = uri("http://artifactory.headout.com/artifactory/absolut-ergo-kotlin/")
            credentials {
                username = System.getenv("JFROG_ARTIFACTORY_USERNAME")
                password = System.getenv("JFROG_ARTIFACTORY_PASSWORD")
            }
            isAllowInsecureProtocol = true
        }
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))
