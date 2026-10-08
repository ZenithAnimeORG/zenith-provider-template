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

tasks.register<Zip>("assembleZpk") {
    dependsOn(tasks.jar)
    archiveFileName.set("${project.name}.zpk")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(tasks.jar.map { it.archiveFile })
    from("manifest.json")
    from("settings.json")
    from("icon.png")
}
