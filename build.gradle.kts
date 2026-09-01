plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.navgraph) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register("clean", Delete::class) {
    description = "Deletes the build directory"
    delete(rootProject.layout.buildDirectory)
}
