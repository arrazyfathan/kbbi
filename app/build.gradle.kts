import com.android.build.api.dsl.ApplicationProductFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinx.kover)
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

fun propertyOrEnv(name: String): String? =
    providers.gradleProperty(name).orNull?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }

val releaseKeystorePath = propertyOrEnv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword = propertyOrEnv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = propertyOrEnv("ANDROID_KEY_ALIAS")
val releaseKeyPassword = propertyOrEnv("ANDROID_KEY_PASSWORD")
val hasReleaseSigning =
    listOf(
        releaseKeystorePath,
        releaseKeystorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { !it.isNullOrBlank() }

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
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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

@Suppress("UnstableApiUsage")
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            val versionName = output.versionName.orNull
            if (versionName != null) {
                val baseName =
                    if (variant.flavorName == "production") {
                        "kbbi-v$versionName-release.apk"
                    } else {
                        "kbbi-${variant.flavorName}-v$versionName-release.apk"
                    }
                output.outputFileName.set(baseName)
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    config.setFrom(rootProject.files("detekt.yml"))
    basePath = rootDir.absolutePath
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}

gradle.taskGraph.whenReady {
    val requestsReleaseVariant =
        allTasks.any { task ->
            task.name.contains("Release", ignoreCase = true)
        }
    val requestsSignedArtifact =
        allTasks.any { task ->
            task.name.startsWith("assemble", ignoreCase = true) ||
                task.name.startsWith("bundle", ignoreCase = true) ||
                task.name.startsWith("package", ignoreCase = true) ||
                task.name.startsWith("sign", ignoreCase = true)
        }

    if (requestsReleaseVariant && requestsSignedArtifact && !hasReleaseSigning) {
        throw GradleException(
            "Release signing is not configured. Provide ANDROID_KEYSTORE_PATH, " +
                "ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD.",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.coordinatorlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.lottie)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.rxbinding)
    implementation(libs.rxandroid)
}
