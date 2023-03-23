import dependencies.implementsCommon

plugins {
    kotlinJvm()
    kotlinDoc()
}

dependencies {
    implementsCommon()
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