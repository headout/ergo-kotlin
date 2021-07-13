rootProject.name = "ergo-parent"

include(
    ":ergo-runtime",
    ":ergo-annotations",
    ":ergo-processor",
    "ergo-service-sqs",
    "ergo-spring",
    "sample"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}