plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.paging.common)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
