plugins {
    id("org.jetbrains.dokka") version "1.9.0"
}

allprojects {
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()

    }
}

subprojects {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets {
            register("main") {
                sourceRoot("src/main")
                includes.from("DOC.md")
            }
        }
    }
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(rootDir.resolve("docs/api"))
}

repositories {
    mavenCentral()
    maven {
        name = "HeadoutHostedRepository"
        url = uri("https://nexus.headout.com/repository/maven-central/")
        credentials {
            username = ""
            password = ""
        }
        isAllowInsecureProtocol = true
    }
}
