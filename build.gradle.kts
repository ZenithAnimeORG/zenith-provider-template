plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example.zenith.provider"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.zenith.provider.sdk)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ksoup)

    testImplementation(libs.zenith.provider.sdk)
    testImplementation(libs.zenith.provider.sdk.testkit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.jar {
    archiveFileName.set("provider.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val d8Exe = findD8Executable()

val dexTask = if (d8Exe != null) {
    tasks.register<Exec>("dexProvider") {
        dependsOn(tasks.jar)
        val jarTask = tasks.jar
        val dexOutputDir = layout.buildDirectory.dir("dex")

        doFirst {
            dexOutputDir.get().asFile.mkdirs()
        }

        inputs.file(jarTask.map { it.archiveFile })
        outputs.file(dexOutputDir.map { it.file("classes.dex") })

        executable(d8Exe.absolutePath)
        argumentProviders.add(CommandLineArgumentProvider {
            listOf(
                "--min-api", "26",
                "--output", dexOutputDir.get().asFile.absolutePath,
                jarTask.get().archiveFile.get().asFile.absolutePath,
            )
        })
    }
} else null

tasks.register<Zip>("assembleZpk") {
    dependsOn(tasks.jar)
    if (dexTask != null) {
        dependsOn(dexTask)
    }
    archiveFileName.set("${project.name}.zpk")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(tasks.jar.map { it.archiveFile })
    if (dexTask != null) {
        from(layout.buildDirectory.dir("dex")) {
            include("classes.dex")
            rename { "provider.dex" }
        }
    }
    from("manifest.json")
    from("settings.json")
    from("icon.png")
}

fun findD8Executable(): File? {
    val candidates = listOfNotNull(
        System.getenv("ANDROID_HOME"),
        System.getenv("ANDROID_SDK_ROOT"),
        System.getProperty("user.home") + "/Android/Sdk",
        "/usr/local/lib/android/sdk",
        "/opt/android-sdk",
    )
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val exeName = if (isWindows) "d8.bat" else "d8"

    for (candidate in candidates) {
        val buildTools = File(candidate, "build-tools")
        if (buildTools.isDirectory) {
            val toolDirs = buildTools.listFiles()?.filter { it.isDirectory }?.sortedDescending() ?: emptyList()
            for (dir in toolDirs) {
                val d8 = File(dir, exeName)
                if (d8.exists() && d8.canExecute()) {
                    return d8
                }
            }
        }
    }

    val pathDirs = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
    for (dir in pathDirs) {
        val d8 = File(dir, exeName)
        if (d8.exists() && d8.canExecute()) {
            return d8
        }
    }
    return null
}
