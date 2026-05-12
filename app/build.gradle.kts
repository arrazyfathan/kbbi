import com.android.build.api.dsl.ApplicationProductFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.gms.google-services")
}

val packageName = "com.arrazyfathan.kbbi"
val appAliasName = "KBBI"
val versionPropertiesFile = file("version.properties")

if (!versionPropertiesFile.canRead()) {
    throw GradleException("Could not read version.properties!")
}

val versionProperties =
    Properties().apply {
        FileInputStream(versionPropertiesFile).use(::load)
    }

val versionMajor = versionProperties["VERSION_MAJOR"].toString().toInt()
val versionMinor = versionProperties["VERSION_MINOR"].toString().toInt()
val versionMaintenance = versionProperties["VERSION_MAINTENANCE"].toString().toInt()
val versionDev = versionProperties["VERSION_DEV"].toString().toInt()
val versionBeta = versionProperties["VERSION_BETA"].toString().toInt()
val versionAlpha = versionProperties["VERSION_ALPHA"].toString().toInt()
val versionCodeValue = versionProperties["VERSION_CODE"].toString().toInt()

fun ApplicationProductFlavor.configureAppMetadata(applicationName: String) {
    resValue("string", "version_code", versionCodeValue.toString())
    resValue("string", "package_name", packageName)
    resValue("string", "app_name", applicationName)
    manifestPlaceholders["application_name"] = applicationName
    buildConfigField("String", "application_name", "\"$applicationName\"")
}

android {
    namespace = packageName
    compileSdk = 37

    defaultConfig {
        applicationId = packageName
        minSdk = 23
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "stage"
    productFlavors {
        create("development") {
            applicationId = "$packageName.dev"
            dimension = "stage"
            versionName =
                if (versionDev == 0) {
                    "$versionMajor.$versionMinor.$versionMaintenance-dev"
                } else {
                    "$versionMajor.$versionMinor.$versionMaintenance-dev.$versionDev"
                }
            resValue("string", "version_name", versionName!!)
            configureAppMetadata("Dev $appAliasName")
        }

        create("production") {
            applicationId = packageName
            dimension = "stage"
            versionName =
                if (versionMaintenance == 0) {
                    "$versionMajor.$versionMinor"
                } else {
                    "$versionMajor.$versionMinor.$versionMaintenance"
                }
            resValue("string", "version_name", versionName!!)
            configureAppMetadata(appAliasName)
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../kbbi_keystore")
            storePassword = "kbbi123"
            keyAlias = "kbbi_key"
            keyPassword = "kbbi123"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            val versionName = output.versionName.orNull
            if (versionName != null) {
                output.outputFileName.set(
                    output.outputFileName.get().replace(".apk", "-$versionName.apk"),
                )
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("com.airbnb.android:lottie:6.7.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.8")

    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")

    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))

    implementation("io.insert-koin:koin-core:4.2.1")
    implementation("io.insert-koin:koin-android:4.2.1")

    implementation("com.jakewharton.rxbinding4:rxbinding:4.0.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
}
