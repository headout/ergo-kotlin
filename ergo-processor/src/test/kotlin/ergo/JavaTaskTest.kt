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

/**
 * Created by shivanshs9 on 21/05/20.
 */
class JavaTaskTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun staticNoArg() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            
            class ExampleTask {
                @Task(taskId="noArg")
                public static boolean noArg() {
                    return true;
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun staticOneArg() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;

            class ExampleTask {
                @Task(taskId="oneArg")
                public int square(int num) {
                    return 1;
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun noArgWithUnitResult() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            
            class ExampleTask {
                @Task(taskId="noArgWithUnitResult")
                public void noArgWithUnitResult() {
                    System.out.println("doing some work...");
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun oneArgWithJobRequest() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            import headout.oss.ergo.models.JobRequest;
            
            class ExampleTask {
                @Task(taskId="oneArgWithJobRequest")
                public int oneArgWithJobRequest(JobRequest<IntRequest> request) {
                    System.out.println("doing some work...");
                    return request.getRequestData().num;
                }
            }
        """.trimIndent()

        val dataSource = """
            package example.tasks;
            
            import kotlinx.serialization.Serializable;
            import headout.oss.ergo.models.JobRequestData;
            
            @Serializable
            public class IntRequest implements JobRequestData {
                public int num;
                
                IntRequest(int num) {
                    this.num = num;
                }
            }
        """.trimIndent()

        val result = compile(source, "IntRequest.java" to dataSource)
        assertResult(result)
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun assertResult(result: KotlinCompilation.Result) {
        result.sourcesGeneratedByAnnotationProcessor.forEach {
            println(it.canonicalPath)
            println(it.readText())
        }
        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun compile(@Language("java") source: String, vararg helpers: Pair<String, String>) =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.java(EXAMPLE_JAVA_FILE, source)) + helpers.map {
                SourceFile.java(
                    it.first,
                    it.second
                )
            }
            annotationProcessors = listOf(TaskProcessor())
            compilerPluginRegistrars = listOf(SerializationComponentRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

    companion object {
        const val EXAMPLE_JAVA_FILE = "ExampleTask.java"
    }
}