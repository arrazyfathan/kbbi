import com.android.build.api.dsl.ApplicationProductFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.navgraph)
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
            resValue("string", "version_name", versionName.orEmpty())
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
            resValue("string", "version_name", versionName.orEmpty())
            configureAppMetadata(appAliasName)
        }
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystorePath.orEmpty())
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
        buildConfig = true
        resValues = true
        compose = true
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

ksp {
    arg("navgraph.annotatedOnly", "true")
}

navgraph {
    variant.set("developmentDebug")
    renderBackend.set(com.github.skydoves.navgraph.gradle.RenderBackend.AUTO)
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
    implementation(project(":core:app-update"))
    implementation(project(":core:di"))
    implementation(project(":core:logging"))
    implementation(project(":core:presentation:designsystem"))
    implementation(project(":core:presentation:ui"))
    implementation(project(":core:utils"))
    implementation(project(":feature:bookmark:presentation"))
    implementation(project(":feature:detail:presentation"))
    implementation(project(":feature:home:data"))
    implementation(project(":feature:home:domain"))
    implementation(project(":feature:home:presentation"))
    implementation(project(":feature:proverb:data"))
    implementation(project(":feature:proverb:domain"))
    implementation(project(":feature:proverb:presentation"))
    implementation(project(":feature:splash:presentation"))
    implementation(project(":feature:words:presentation"))
    implementation(project(":feature:settings:data"))
    implementation(project(":feature:settings:domain"))
    implementation(project(":feature:settings:presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.lottie)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.timber)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.androidx.compose)
    implementation(libs.lottie.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
