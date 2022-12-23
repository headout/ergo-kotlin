import dependencies.*

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
            name = "ArtifactoryPackages"
            url = uri("http://artifactory.headout.com/artifactory/absolut-ergo-kotlin/")
            credentials {
                username = "artifactory"
                password = "XK3AXQ4zhBfcn4y#"
            }
            isAllowInsecureProtocol = true
        }
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))