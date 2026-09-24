plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "kbbi.android.application"
            implementationClass = "KbbIAndroidApplicationPlugin"
        }
        register("androidLibrary") {
            id = "kbbi.android.library"
            implementationClass = "KbbIAndroidLibraryPlugin"
        }
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
}
