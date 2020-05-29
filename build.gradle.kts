allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    ext {
        set("deps.auto", "com.google.auto.service:auto-service:1.0-rc7")
        set("deps.kotlin-metadata", "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0")
        set("deps.kotlinpoet", "com.squareup:kotlinpoet:1.5.0")
        set("deps.kotlinpoet-metadata", "com.squareup:kotlinpoet-metadata:1.5.0")
        set("deps.truth", "com.google.truth:truth:1.0.1")
        set("deps.junit", "junit:junit:4.13")
        set("deps.compile-testing", "com.github.tschuchortdev:kotlin-compile-testing:1.2.8")
        set("deps.serialization", "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
        set("deps.coroutine", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
        set("deps.coroutine-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.7")
    }
}

repositories {
    mavenCentral()
    jcenter()
}