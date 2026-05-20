# KBBI

KBBI is an unofficial Android dictionary app for **Kamus Besar Bahasa Indonesia**. The app combines a remote dictionary API with a bundled local word index so users can search words quickly, inspect meanings in detail, and keep a personal bookmark/history collection on-device.

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a>
</p>

<p align="center">
  <img src="media/Final.png" alt="KBBI app preview" />
</p>

## Overview

This repository contains the Android client for KBBI. The current project setup is:

- Single Android application module: `:app`
- Kotlin + XML/View system UI
- MVVM + repository pattern
- Koin for dependency injection
- Room for local persistence
- Retrofit + OkHttp for API access
- Navigation Component with a bottom-navigation based main flow
- Product flavors for `development` and `production`
- Static analysis with Detekt and Ktlint
- GitHub Actions CI and Fastlane automation

## Features

- Search Indonesian words from the home screen
- Browse a bundled word list from `app/src/main/assets/entries.json`
- View detailed word entries and meanings
- Save bookmarked words locally
- Keep recent search history locally
- Animated splash screen and motion-based UI touches
- Edge-to-edge system bar support

<p align="right">
  <img src="media/preview.gif" alt="Animated preview" width="32%" />
</p>

## Tech Stack

- **Language:** Kotlin
- **Build tooling:** Gradle, Android Gradle Plugin `9.2.1`, Kotlin `2.3.21`
- **Java target:** Java 17
- **Minimum SDK:** 23
- **Target/Compile SDK:** 37
- **UI:** Android Views, ViewBinding, Material Components, MotionLayout, Lottie
- **Architecture:** MVVM, Repository pattern, UseCase layer
- **Async/data:** Coroutines, LiveData, RxBinding
- **Local storage:** Room
- **Networking:** Retrofit, Gson, OkHttp logging interceptor
- **Dependency injection:** Koin `4.2.1`
- **Code quality:** Detekt, Ktlint
- **Distribution/automation:** Fastlane, GitHub Actions

## Project Structure

```text
.
├── app/
│   ├── src/main/java/com/arrazyfathan/kbbi/
│   │   ├── core/               # Data, domain, repository, Room, Retrofit
│   │   ├── di/                 # App-level Koin modules
│   │   ├── presentation/       # Activities, fragments, adapters, custom views
│   │   └── utils/              # Extensions and window/system-bar helpers
│   ├── src/main/assets/
│   │   └── entries.json        # Bundled word list used by Word List screen
│   ├── src/main/res/           # XML layouts, drawables, animations, navigation
│   ├── build.gradle.kts
│   └── version.properties      # Version naming and versionCode inputs
├── fastlane/                   # Release/distribution automation
├── gradle/libs.versions.toml   # Centralized dependency and plugin versions
└── .github/workflows/          # CI and tagged release pipeline
```

## Application Flow

The app currently has three main destinations inside the bottom navigation:

1. **Home**  
   Search for a word, see loading/error states, and store recent searches.
2. **Word List**  
   Filter the bundled local word index from `entries.json`, then fetch details from the API.
3. **Bookmarks**  
   View and remove locally saved entries.

Detailed meaning results open in `DetailActivity`, where users can bookmark or remove saved words.

## Data Sources

### Remote API

- Base URL: `https://kbbi-api-green.vercel.app`
- API repository: <https://github.com/arrazyfathan/kbbi-api>

### Local Data

- **Room database:** stores history and bookmark data in `kbbi_db`
- **Asset file:** `app/src/main/assets/entries.json` provides the local searchable word list

## Requirements

To build the project locally, use:

- Android Studio with current Android SDK tooling
- JDK 17
- Android SDK Platform 37
- An Android device or emulator for runtime testing

## Getting Started

### 1. Clone the repository

```sh
git clone https://github.com/arrazyfathan/kbbi.git
cd kbbi
```

### 2. Make sure local Android SDK is configured

Your `local.properties` should point to a valid Android SDK installation, for example:

```properties
sdk.dir=/path/to/Android/sdk
```

### 3. Sync dependencies

```sh
./gradlew help
```

If Gradle sync succeeds, the project is ready to open in Android Studio.

## Build Variants

The app defines one flavor dimension, `stage`, with two product flavors:

- `development`
  Uses application ID `com.arrazyfathan.kbbi.dev`
- `production`
  Uses application ID `com.arrazyfathan.kbbi`

Examples:

```sh
./gradlew assembleDevelopmentDebug
./gradlew assembleProductionDebug
```

Release APK names are customized automatically:

- Production: `kbbi-v<version>-release.apk`
- Non-production: `kbbi-<flavor>-v<version>-release.apk`

Version values are driven by [`app/version.properties`](app/version.properties).

## Running the App

For local development, the normal entry point is the development debug build:

```sh
./gradlew installDevelopmentDebug
```

Or from Android Studio, choose the `developmentDebug` variant and run the `app` configuration.

## Code Quality and Testing

The project contains two suites of tests:
1. **Local Unit Tests** (located in `app/src/test`): Test pure logic and business interactors using mock/fake repositories directly on the host JVM.
2. **Instrumented Integration Tests** (located in `app/src/androidTest`): Test database operations and Android-dependent integrations on a physical device or emulator.

### Running Local Unit Tests
To run JVM unit tests:
```sh
./gradlew testDevelopmentDebugUnitTest
```

### Running Instrumented Integration Tests
To compile instrumented tests without running them:
```sh
./gradlew compileDevelopmentDebugAndroidTestKotlin
```

To run instrumented tests (requires a running emulator or connected device):
```sh
./gradlew connectedDevelopmentDebugAndroidTest
```

### Checking Test Coverage
We use the JetBrains Kover plugin to measure unit test coverage. Since the project includes product flavors and signing rules, run the coverage tasks specifically for the development debug variant:

- **Console Summary**: Print coverage statistics directly to the terminal:
  ```sh
  ./gradlew app:koverLogDevelopmentDebug
  ```
- **HTML Report**: Generate a detailed HTML report:
  ```sh
  ./gradlew app:koverHtmlReportDevelopmentDebug
  ```
  The report is saved at `app/build/reports/kover/htmlDevelopmentDebug/index.html`.

### Quality and Validation Commands
Other useful check tasks:
```sh
./gradlew lintDevelopmentDebug
./gradlew detekt
./gradlew ktlintCheck
```

You can run a broader validation pass including unit tests and build tasks:
```sh
./gradlew testDevelopmentDebugUnitTest lintDevelopmentDebug assembleDevelopmentDebug
```

## Release Signing

Release builds are intentionally blocked unless signing is configured. The Gradle build expects these values through either Gradle properties or environment variables:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Example:

```sh
export ANDROID_KEYSTORE_PATH=/absolute/path/to/release.keystore
export ANDROID_KEYSTORE_PASSWORD=your-store-password
export ANDROID_KEY_ALIAS=your-key-alias
export ANDROID_KEY_PASSWORD=your-key-password
./gradlew assembleProductionRelease
```

Without those values, any signed release packaging task will fail by design.

## Fastlane

The repository already includes Fastlane setup.

Install Ruby dependencies:

```sh
bundle install
```

Available lanes:

```sh
bundle exec fastlane android test
```

Current behavior:

- `android test` runs Gradle tests

## CI/CD

GitHub Actions workflow: `.github/workflows/android.yml`

### Pull requests and pushes to `main`

The `validate` job runs:

```sh
./gradlew testDevelopmentDebugUnitTest lintDevelopmentDebug assembleDevelopmentDebug --stacktrace
```

If successful, it uploads the development debug APK as a workflow artifact.

### Tagged releases

When a git tag is pushed, the `release` job:

1. Validates signing secrets
2. Builds `assembleProductionRelease`
3. Uploads the signed production APK as an artifact
4. Publishes a GitHub release with generated release notes

Required GitHub secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Design and UX Notes

The current app includes:

- Custom splash screen animation
- Motion/transition-driven interactions on the home screen
- Lottie-based loading and empty states
- Custom system bar and inset handling helpers

## Screenshots and Metrics

### MAD Score

![summary](media/summary.png "Summary")
![kotlin](media/kotlin.png "Kotlin")
![jetpack](media/jetpack.png "Jetpack")

## Download

Latest published APK:

- <https://github.com/arrazyfathan/kbbi/releases/download/1.0/app-release.apk>

## License

```text
Designed and developed by 2022 arrazyfathan (Ar Razy Fathan Rabbani)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

