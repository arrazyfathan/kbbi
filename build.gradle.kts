plugins {
    id("com.android.application") version "9.2.0" apply false
    id("com.android.legacy-kapt") version "9.2.0" apply false
    id("androidx.navigation.safeargs") version "2.9.8" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.21" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
