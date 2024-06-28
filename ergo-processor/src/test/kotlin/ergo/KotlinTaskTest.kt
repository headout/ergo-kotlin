package ergo

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import headout.oss.ergo.processors.TaskProcessor
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationComponentRegistrar
import org.junit.Test
import java.util.concurrent.CompletableFuture

/**
 * Created by shivanshs9 on 21/05/20.
 */
class KotlinTaskTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun staticNoArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="noArg")
                @JvmStatic
                fun noArg(): Boolean = true
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun noArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            class ExampleTask {
                @Task(taskId="noArg")
                fun noArg(): Boolean = true
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun oneArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="oneArg")
                @JvmStatic
                fun square(num: Int) = num * num 
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun suspendNoArg() {

        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            import headout.oss.ergo.listeners.JobCallback
            import kotlinx.coroutines.delay
            
            class ExampleTask {
                @Task(taskId="suspendNoArg")
                suspend fun longProcess(): String {
                    delay(2000)
                    return "hello world"
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun suspendOneArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            import headout.oss.ergo.listeners.JobCallback
            
            class ExampleTask {
                @Task(taskId="suspendOneArg")
                suspend fun longProcess(num: Int) = num * num 
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun noArgWithNonSerializableResult() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="noArgWithNonSerializableResult")
                @JvmStatic
                fun noArgWithNonSerializableResult(): Result = Result(10)
            }
            
            data class Result(val number: Int)
        """.trimIndent()

        val result = compile(source)
        assertResult(result, exitCode = ExitCode.COMPILATION_ERROR)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun noArgWithSerializableResult() {
        val source = """
            package example.tasks
            
            import kotlinx.serialization.Serializable
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                fun noArgWithSerializableResult(): Result = Result(10)
            }
            
            @Serializable
            data class Result(val number: Int)
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun noArgWithUnitResult() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            class ExampleTask {
                @Task(taskId="noArgWithUnitResult")
                fun noArgWithUnitResult(): Unit {
                    println("doing some work...")
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    // Default Value is not yet supported in kotlinpoet so the
    // generated request data class is actually invalid
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun oneArgWithDefaultValue() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            class ExampleTask {
                @Task(taskId="oneArgWithDefaultValue")
                fun oneArgWithDefaultValue(num: Int = 134) {
                    println("doing some work x $\num...")
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun assertResult(result: KotlinCompilation.Result, exitCode: ExitCode = ExitCode.OK) {
        result.sourcesGeneratedByAnnotationProcessor.forEach {
            println(it.canonicalPath)
            println(it.readText())
        }
        assertThat(result.exitCode).isEqualTo(exitCode)
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun compile(@Language("kotlin") source: String) = KotlinCompilation().apply {
        sources = listOf(SourceFile.kotlin(EXAMPLE_KOTLIN_FILE, source))
        annotationProcessors = listOf(TaskProcessor())
        inheritClassPath = true
        compilerPluginRegistrars = listOf(SerializationComponentRegistrar())
        messageOutputStream = System.out
    }.compile()

    companion object {
        const val EXAMPLE_KOTLIN_FILE = "ExampleTask.kt"
    }
}