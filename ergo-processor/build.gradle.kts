import dependencies.*

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