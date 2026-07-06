import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.kover)
}

fun requiredBaseUrl(): String {
    val localProperties =
        Properties().apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.canRead()) {
                localPropertiesFile.inputStream().use(::load)
            }
        }

    val baseUrl =
        providers.gradleProperty("KBBI_BASE_URL").orNull?.takeIf { it.isNotBlank() }
            ?: providers.environmentVariable("KBBI_BASE_URL").orNull?.takeIf { it.isNotBlank() }
            ?: localProperties.getProperty("KBBI_BASE_URL")?.takeIf { it.isNotBlank() }

    require(!baseUrl.isNullOrBlank()) {
        "KBBI_BASE_URL is required. Add it to local.properties, pass -PKBBI_BASE_URL=..., or set the KBBI_BASE_URL environment variable."
    }
    require(!baseUrl.contains('"') && !baseUrl.contains('\\')) {
        "KBBI_BASE_URL must not contain quotes or backslashes."
    }

    return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}

android {
    namespace = "com.arrazyfathan.kbbi.core.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"${requiredBaseUrl()}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    api(project(":core:domain"))

    implementation(project(":core:logging"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
}
