# KBBI

KBBI is an unofficial Android dictionary app for **Kamus Besar Bahasa Indonesia**. The app combines a remote dictionary API with a bundled local word index and an offline-first Room cache so users can search words quickly, revisit previously opened meanings without a network connection, and keep bookmarks and recent search history on-device.

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a>
</p>

<p align="center">
  <img src="media/Final.png" alt="KBBI app preview" />
</p>

## Overview

This repository contains the Android client for KBBI. The project is organized as a multi-module Clean Architecture codebase:

- `:app` owns application startup, Koin assembly, and the root Navigation3 graph.
- `:core:*` contains shared data, domain, logging, presentation, and utility code.
- `:feature:*` contains feature-specific presentation, data, and domain modules.
- Feature navigation is exposed through each feature's `navigation` package and wired from `:app`.
- Presentation uses Jetpack Compose, ViewModels, state, and one-shot UI events.
- Data access uses repositories, use cases, Room, Ktor, OkHttp, and kotlinx.serialization.
- Dependency injection uses Koin modules composed at the app boundary.
- Product flavors are available for `development` and `production`.

## Features

- Search Indonesian words from the home screen
- Browse a bundled local word list from `feature/home/data/src/main/assets/entries.json`
- View detailed word entries and meanings
- Cache successful meaning lookups locally, including words opened from the word list
- Browse and search Indonesian proverbs with paged results
- Cache proverb pages and proverb meanings for fallback when the remote API is unavailable
- Save bookmarked words locally
- Keep recent search history locally
- Animated splash screen and Lottie-based loading/empty states
- Shared design system, UI text handling, error mapping, networking, and logging

<p align="right">
  <img src="media/preview.gif" alt="Animated preview" width="32%" />
</p>

## Tech Stack

- **Language:** Kotlin
- **Build tooling:** Gradle, Android Gradle Plugin `9.2.1`, Kotlin `2.4.0`
- **Java target:** Java 17
- **Minimum SDK:** 23
- **Target/Compile SDK:** 37
- **UI:** Jetpack Compose, Material 3, Lottie Compose
- **Navigation:** AndroidX Navigation3
- **Architecture:** Clean Architecture, MVVM-style presentation, Repository pattern, UseCase layer
- **Async/data:** Coroutines, StateFlow, Channel-based events
- **Local storage:** Room `2.8.4`
- **Networking:** Ktor Client, kotlinx.serialization, OkHttp engine/logging
- **Dependency injection:** Koin `4.2.2`
- **Code quality:** Detekt, Ktlint
- **Distribution/automation:** Fastlane, GitHub Actions

## Module Structure

```text
.
├── app/
│   └── src/main/java/com/arrazyfathan/kbbi/
│       ├── BaseApplication.kt       # Koin startup
│       ├── MainActivity.kt          # Activity entry point
│       ├── di/                      # App-level module assembly
│       └── navigation/              # Root app navigation graph
├── core/
│   ├── data/                        # Shared Ktor client and safe API call helpers
│   ├── di/                          # Shared core Koin modules
│   ├── domain/                      # AppResult, DataError, shared domain primitives
│   ├── logging/                     # App and network logging helpers
│   ├── presentation/
│   │   ├── designsystem/            # Theme, colors, type, icons, resources, components
│   │   └── ui/                      # UiText, DataErrorToText, shared UI helpers
│   └── utils/                       # System bar and platform utilities
├── feature/
│   ├── bookmark/
│   │   └── presentation/            # Bookmark screen, ViewModel, route
│   ├── detail/
│   │   └── presentation/            # Detail screen, ViewModel, route
│   ├── home/
│   │   ├── data/                    # Room, remote/local data sources, repository impl
│   │   ├── domain/                  # Word models, repositories, use cases
│   │   └── presentation/            # Home screen, ViewModel, route
│   ├── proverb/
│   │   ├── data/                    # Paging, Room cache, remote data source
│   │   ├── domain/                  # Proverb models, repository, use cases
│   │   └── presentation/            # Proverb screen, ViewModel, route
│   ├── splash/
│   │   └── presentation/            # Splash screen and route
│   └── words/
│       └── presentation/            # Word list screen, ViewModel, route
├── fastlane/                        # Release/distribution automation
├── gradle/libs.versions.toml        # Centralized dependency and plugin versions
└── .github/workflows/               # CI and tagged release pipeline
```

## Architecture

The dependency direction is kept inward:

```text
app
 ├── feature:*:presentation
 ├── core:di
 └── core:presentation:designsystem

feature:*:presentation
 ├── feature:home:domain
 ├── core:domain
 ├── core:presentation:ui
 └── core:presentation:designsystem

feature:home:data
 ├── feature:home:domain
 ├── core:data
 ├── core:domain
 └── core:logging

feature:proverb:data
 ├── feature:proverb:domain
 ├── core:data
 └── core:domain

core modules
 └── shared primitives with no feature ownership
```

`HttpClientFactory` and `SafeApiCall` live in `:core:data` so every feature data module can reuse the same network setup. `UiText` and `DataErrorToText` live in `:core:presentation:ui` so presentation modules can map domain/data errors to localized UI messages consistently.

Word meaning lookup is handled by `WordRepository` in `:feature:home:data`. It checks Room first, returns cached meanings when present, calls the remote API only on cache miss, and stores successful remote responses back into Room. Cached meanings and bookmarks share `word_table`; `isSaved = false` means the word is cached only, while `isSaved = true` means it also appears in the bookmark list.

Proverb lookup is handled by `NetworkProverbRepository` in `:feature:proverb:data`. Proverb lists are loaded with Paging, successful pages are cached in `proverb_db`, and cached pages are used when a remote page load fails. Proverb detail requests are remote-first; successful detail responses are cached, and cached detail is returned when the remote request fails.

## Application Flow

The app starts at the splash destination, then enters the main flow owned by the root navigation graph in `:app`.

- **Home:** Search words, display loading/error states, cache successful meanings, and store recent searches.
- **Words:** Filter the bundled local word index, then open word details through the same cache-aware lookup flow.
- **Proverbs:** Search and page through proverb results, then open proverb meanings with cached fallback.
- **Bookmarks:** View saved entries and remove them from bookmarks without deleting cached meanings.
- **Detail:** Show meanings for a selected word and toggle bookmark state.

Each feature exposes its route from a `navigation` package, while `AppNavigation` in `:app` composes those destinations into the app graph.

## Data Sources

### Remote API

- Base URL: `https://kbbi-api-green.vercel.app`
- API repository: <https://github.com/arrazyfathan/kbbi-api>
- Shared network helpers: `core/data/src/main/java/com/arrazyfathan/kbbi/core/data/remote/network`

### Local Data

- **Room database:** stores cached meanings, bookmark flags, and search history in `kbbi_db`
- **Proverb Room database:** stores cached proverb pages and cached proverb details in `proverb_db`
- **Asset file:** `feature/home/data/src/main/assets/entries.json` provides the local searchable word list. It contains entries only, not definitions.

Current word lookup behavior:

1. The app normalizes and validates the query in `SearchWordUseCase`.
2. `WordRepository` checks Room for an existing `word_table` row.
3. If cached, the app opens the detail screen from local data.
4. If missing, the repository requests `/search/{word}` from the remote API.
5. Successful remote responses are stored in Room with `isSaved = false`.
6. Bookmarking the word upserts the same cached payload with `isSaved = true`.
7. Removing a bookmark sets `isSaved = false`, preserving the cached meaning for offline lookup.

Current proverb lookup behavior:

1. `ProverbViewModel` debounces the search query and requests paged results through `GetListProverbsUseCase`.
2. `ProverbPagingSource` loads pages from the remote API.
3. Successful page responses replace the cached page data for the query in `proverb_db`.
4. If a remote page load fails, the paging source returns the cached page when available.
5. Tapping a proverb requests detail through `GetProverbMeaningUseCase`.
6. Successful detail responses are cached by slug.
7. If a detail request fails, cached detail is returned when available.

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

### 2. Configure the Android SDK

Your `local.properties` should point to a valid Android SDK installation:

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

- `development` uses application ID `com.arrazyfathan.kbbi.dev`
- `production` uses application ID `com.arrazyfathan.kbbi`

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

## Testing

Local unit tests are split across the app, core, and feature modules. Instrumented tests currently live under the feature data layer where Android-dependent Room behavior is validated.

Run JVM unit tests:

```sh
./gradlew testDevelopmentDebugUnitTest :core:logging:testDebugUnitTest :core:domain:test :feature:home:data:testDebugUnitTest :feature:home:domain:test
```

Compile Android tests:

```sh
./gradlew :feature:home:data:compileDebugAndroidTestKotlin
```

Focused compile checks for the current cache and word-list flow:

```sh
./gradlew --no-daemon :feature:home:data:compileDebugKotlin :feature:home:data:compileDebugAndroidTestKotlin
./gradlew --no-daemon :feature:words:presentation:compileDebugKotlin :app:compileDevelopmentDebugKotlin
```

Run Android tests on a connected device or emulator:

```sh
./gradlew :feature:home:data:connectedDebugAndroidTest
```

## Coverage

The project uses the JetBrains Kover plugin. For the development debug variant:

```sh
./gradlew app:koverLogDevelopmentDebug
./gradlew app:koverHtmlReportDevelopmentDebug
```

The HTML report is generated at `app/build/reports/kover/htmlDevelopmentDebug/index.html`.

## Quality and Validation

Useful validation commands:

```sh
./gradlew lintDevelopmentDebug
./gradlew detekt
./gradlew ktlintCheck
./gradlew assembleDevelopmentDebug
```

A focused post-refactor validation pass:

```sh
./gradlew testDevelopmentDebugUnitTest :core:logging:testDebugUnitTest :core:domain:test :feature:home:data:testDebugUnitTest :feature:home:domain:test :feature:home:data:compileDebugAndroidTestKotlin
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

The repository includes Fastlane setup.

Install Ruby dependencies:

```sh
bundle install
```

Available lane:

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

- Compose-first screen implementation
- Material 3 components and app theming
- Shared design system module for theme, type, colors, icons, resources, and components
- Shared `UiText` and `DataErrorToText` for localized presentation messages
- Custom splash screen animation
- Lottie-based loading and empty states
- AndroidX edge-to-edge system bar setup
- Compose previews for key screens and content components

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
