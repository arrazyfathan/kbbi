plugins {
    id("kbbi.android.library")
    alias(libs.plugins.kotlinx.kover)
}

android {
    namespace = "com.arrazyfathan.kbbi.feature.settings.data"
}

dependencies {
    implementation(project(":feature:settings:domain"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    testImplementation(libs.junit)
}
