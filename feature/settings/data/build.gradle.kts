plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.kover)
}

android {
    namespace = "com.arrazyfathan.kbbi.feature.settings.data"
    compileSdk = 37
    defaultConfig { minSdk = 23 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":feature:settings:domain"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    testImplementation(libs.junit)
}
