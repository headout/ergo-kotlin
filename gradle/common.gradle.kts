val VERSION_NAME = "1.3.1-beta"

version = VERSION_NAME
group = "headout.oss"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")
