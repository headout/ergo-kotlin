plugins {
    id("org.jetbrains.dokka") version "1.9.10"
}

allprojects {
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        jcenter()
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
}
