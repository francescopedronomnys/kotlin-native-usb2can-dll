plugins {
    kotlin("multiplatform") version "1.6.10"
}

repositories {
    mavenCentral()
}

val WIN_LIBRARY_PATH =
    "c:\\msys64\\mingw64\\bin;c:\\Tools\\msys64\\mingw64\\bin;C:\\Tools\\msys2\\mingw64\\bin"

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")

    val nativeTarget = when {
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {

        val OS_NAME = System.getProperty("os.name").toLowerCase()

        val HOST_NAME = when {
            OS_NAME.startsWith("linux") -> "linux"
            OS_NAME.startsWith("windows") -> "windows"
            OS_NAME.startsWith("mac") -> "macos"
            else -> error("Unknown os name `$OS_NAME`")
        }

        val paths = if (HOST_NAME == "windows") {
            listOf(
                "C:/msys64/mingw64/include/usbtocan",
                "C:/Tools/msys64/mingw64/include/usbtocan",
                "C:/Tools/msys2/mingw64/include/usbtocan"
            )
        } else {
            error("Unknown host name `$HOST_NAME`")
        }

        binaries {
            executable {
                entryPoint = "main"
            }
        }

        this.compilations.getByName("main") {
            cinterops.create("libusbtocan") {
                defFile = File(projectDir, "src/nativeInterop/cinterop/libusbtocan.def")
                includeDirs.headerFilterOnly(paths)

                afterEvaluate {
                    if (this.name == "mingwX64") {
                        val winTests =
                            tasks.getByName("mingwX64Test") as org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
                        winTests.environment("PATH", WIN_LIBRARY_PATH)
                    }
                }
            }
        }

    }
}
