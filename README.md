## Ergo [![Release](https://jitpack.io/v/headout/ergo-kotlin.svg)](https://jitpack.io/#headout/ergo-kotlin) [![Ergo client CI](https://github.com/headout/ergo-kotlin/workflows/Ergo%20client%20CI/badge.svg)](https://github.com/headout/ergo-kotlin/actions?query=workflow%3A"Ergo+client+CI")

### Introduction

Ergo is a client library to run application tasks on a shared pool of workers. It is a generic implementation to handle offloading tasks. "Services" are written to actually receive the message and then the runtime "Job Controller" actually runs the relevant functions annotated with the provided "taskId".

### Why use it?

If you've got long-running tasks, implemented in Java/Kotlin, that you'd like to run from any other microservice, then you can benefit from the declarative and ease-of-use functionality of Ergo.
More likely than not, since gRPC and HTTP calls would just timeout for really long-running tasks, Ergo SQS service would provide a feasible approach to use SQS for communication of task requests and executed job results.
Even if you'd like to use some service other than SQS for communication, you can make use of core Ergo logic, in [ergo-runtime](ergo-runtime) project, to implement your own custom service.

### Installation

#### Add dependencies to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (required by the generated code):

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
repositories {
    // artifacts are published to JCenter
    jcenter()
    maven(url="https://jitpack.io")
}

plugins {
  kotlin("plugin.serialization") version kotlinVersion
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION)) // or "stdlib-jdk8"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2") // JVM dependency
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
repositories {
    // artifacts are published to JCenter
    jcenter()
    maven { url "https://jitpack.io" }
}

plugins {
  id 'org.jetbrains.kotlin.plugin.serialization' version kotlinVersion
}

dependencies {
  implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version" // or "kotlin-stdlib-jdk8"
  implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2" // JVM dependency
}
```

</details>

#### Add dependencies to Ergo annotation processor:

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
plugins {
  kotlin("kapt") version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt("com.github.headout.ergo-kotlin:ergo-processor:1.2.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
plugins {
  id 'org.jetbrains.kotlin.kapt' version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt "com.github.headout.ergo-kotlin:ergo-processor:1.2.0"
}
```

</details>

#### Using SQS queue for receiving tasks

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation(platform("software.amazon.awssdk:bom:2.13.26"))
  implementation("software.amazon.awssdk:sqs")
  implementation("com.github.headout.ergo-kotlin:ergo-service-sqs:1.2.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation platform("software.amazon.awssdk:bom:2.13.26")
  implementation "software.amazon.awssdk:sqs"
  implementation "com.github.headout.ergo-kotlin:ergo-service-sqs:1.2.0"
}
```

</details>

#### Optional: Using Spring Services and Autowired properties

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation("com.github.headout.ergo-kotlin:ergo-spring:1.2.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation "com.github.headout.ergo-kotlin:ergo-spring:1.2.0"
}
```

</details>

### Documentation

Refer to [Samples](sample) to check out example usages.

### Architecture

#### Terminology

1. **Task =>** used to denote an executable function with given input and given output
  - Has TaskId to uniquely differentiate tasks
  - Can be used to refer to both regular and suspending functions
  - Function Parameters must be serializable (using @Serializable on the data class)
  - The task function can either be regular or suspending function, and its return type is the job result type too (which must again be serializable)
2. **TaskId =>** name to map to a particular function (must be unique in project).
  - For SQS FIFO queues, it is analogous to **MessageGroupId**
  - For Pulsar, it is analogous to **Topic**
3. **JobId =>** uniquely generated from the sender side to denote a particular running instance of a task.
  - For SQS queues, it is analogous to **MessageId**
