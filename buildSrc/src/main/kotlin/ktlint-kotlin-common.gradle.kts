import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    // Do not enable explicit api for cli project
    if (project.name != "ktlint") {
        explicitApiWarning()
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf("-Xuse-k2")
    }
}

addAdditionalJdkVersionTests()
