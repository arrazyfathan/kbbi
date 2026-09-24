import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kbbi.android.library")
    alias(libs.plugins.kotlinx.kover)
}

android {
    namespace = "com.arrazyfathan.kbbi.core.di"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:observability"))
    implementation(libs.koin.core)
    implementation(libs.ktor.client.core)
    implementation(libs.kotlinx.serialization.json)
}
