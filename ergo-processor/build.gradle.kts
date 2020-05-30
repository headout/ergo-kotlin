plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

kapt {
    generateStubs = true
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(rootProject.ext["deps.serialization"] as String)
    implementation(project(":ergo-annotations"))
    implementation(project(":ergo-runtime"))
    implementation(rootProject.ext["deps.auto"] as String)
    kapt(rootProject.ext["deps.auto"] as String)
    implementation(rootProject.ext["deps.kotlin-metadata"] as String)
    implementation(rootProject.ext["deps.kotlinpoet-metadata"] as String)
    implementation(rootProject.ext["deps.kotlinpoet"] as String)

//    testImplementation("org.jetbrains.kotlin:plugin.serialization")
    testImplementation(rootProject.ext["deps.kotlin-metadata"] as String)
    testImplementation(rootProject.ext["deps.truth"] as String)
    testImplementation(rootProject.ext["deps.junit"] as String)
    testImplementation(rootProject.ext["deps.compile-testing"] as String)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))