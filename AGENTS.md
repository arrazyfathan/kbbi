# Guidance for agents working on KBBI

## Working conventions

- This is a native Kotlin Android app. Follow the existing feature and layer structure before introducing abstractions, libraries, or modules.
- Inspect the nearest implementation, its Gradle dependencies, DI registration, and tests before editing. Use executable code and tests as the source of truth when README or planning documents disagree.
- Treat `plan/`, `planning.md`, and `docs/future-development.md` as proposals or historical context, not proof that a feature exists.
- Keep changes within the requested scope. Preserve unrelated working-tree edits, and avoid broad formatting, dependency upgrades, or architectural migrations as part of a feature change.
- Follow explicit user requirements over defaults in this document. Update this guidance when an intentional architecture change makes it inaccurate.
- Keep this file focused on durable conventions. Record temporary failures in issues or task notes, and revise affected guidance and reference links when the architecture intentionally changes.

## Module boundaries

- `:app` owns application startup, module assembly, Navigation3 integration, external intents, and Android adapters such as notifications, widgets, and app icons.
- `:feature:<name>:domain` contains domain models, repository/service contracts, and use cases. Keep new business logic independent of Android context, Compose, Ktor, Room, and implementation classes.
- `:feature:<name>:data` implements domain contracts and owns DTOs, mappers, remote/local sources, Room, and DataStore.
- `:feature:<name>:presentation` contains screen state, actions, events, ViewModels, and Compose UI. Depend on domain contracts/use cases; do not import feature data implementations.
- Reuse the home domain for shared word capabilities. Bookmark, detail, and words currently have presentation modules that consume it; do not create duplicate word repositories for each screen.
- `:feature:wordstudy` owns AI provider/configuration and word-study models, contracts, use cases, data sources, encrypted persistence, and settings presentation. Detail may map Home word models into its source boundary but must not make Word Study depend on Home.
- Shared result/error types live in `:core:domain`; networking in `:core:data`; theme/resources/components in `:core:presentation:designsystem`; UI text/errors/loading coordination in `:core:presentation:ui`; reporting contracts/adapters in `:core:observability`.
- Use existing module layouts rather than adding empty layers. Domain modules already have selected dependencies such as serialization and Paging; do not migrate these incidental choices during unrelated work.

## Domain and data conventions

- Follow the established path: **use case → domain repository interface → data repository → remote/local data source**.
- Domain names follow `*Model`, `*Repository`, and `*UseCase`. Use cases use constructor injection and `operator fun invoke`, with `suspend` for one-shot asynchronous operations. Normalize and validate caller input here when it is domain behavior.
- Use `AppResult<T, DataError>` and existing helpers for fallible operations. Do not add a competing `Result`, error hierarchy, or exception-based public API without a concrete need.
- Repository interfaces belong in domain `repository/`. Concrete sources belong in data `source/remote/` or `source/local/`; match names such as `WordRemoteDataSource`, `TopWordsRemoteDataSource`, and `WordLocalDataSource`. Do not add a `Ktor` prefix or duplicate domain data-source interface for this pattern.
- `WordRepository` implements several focused contracts, including word search, bookmarks, history, translation, and top words. Extend this pattern for related word behavior. Other features can have their own implementation, such as `NetworkProverbRepository`; do not rename existing classes for stylistic uniformity.
- Keep serializable API DTOs in `source/remote/dto/`, Room entities in `source/local/entity/`, and DAOs/databases in `source/local/room/`. Keep DTOs, entities, and domain models separate.
- Put extension mappers in the data `mapper/` package, using nearby names such as `toDomain()` and `toEntity()`. Remote sources currently map API payloads into domain results before returning them to repositories.
- Inject the shared `HttpClient`. Reuse `HttpClientFactory` and the [safe request helpers](core/data/src/main/java/com/arrazyfathan/kbbi/core/data/remote/network/SafeApiCall.kt); do not construct another production client for a feature.
- Check the actual endpoint contract before adding a request: path, envelope, parameters, nullability, and empty-result semantics. Routes currently include both unversioned paths and `/api/v1/words/top`; do not globally change route prefixes.
- Preserve required versus optional fields and use numeric types that fit the API contract. Do not silently turn missing required fields or malformed responses into empty successes. Keep automated contract tests deterministic with representative fixtures and `MockEngine`; live backend checks are separate verification.
- Handle API envelope failures as `DataError.Remote(message)` and reuse shared HTTP/network error mapping. An empty list can be success; missing data and not-found behavior must follow the endpoint semantics. Never swallow coroutine cancellation in broad catches or `runCatching` around suspend work.
- Reuse `VisitorIdProvider` for search visit tracking. Do not attach visitor IDs to unrelated requests or reporting events.

## Persistence and concurrency

- Preserve each operation's cache policy. Word lookup currently requests remote data first, stores successful responses while preserving bookmark state, and falls back to Room on failure. Proverb pages/details also support cache fallback. Top words and translation currently delegate directly to remote sources.
- The bundled `entries.json` is a word index, not an offline database of definitions. Keep catalog loading separate from meaning lookup.
- Removing a bookmark must preserve its cached meaning. Changes to history or caches must not unintentionally erase saved words.
- For Room schema changes, update the database version and provide a migration that preserves user data. Existing destructive-fallback configuration is not a substitute for a migration.
- Use the existing DataStore implementations and domain contracts for preferences.
- Use `viewModelScope` for ViewModel work, suspend functions for one-shot reads, and `Flow` for ongoing observations. Cancel superseded work where the current feature does so. Avoid `GlobalScope`, unmanaged scopes, and production `runBlocking`.
- Keep blocking file/database work off the main thread using the surrounding implementation's dispatcher pattern. Do not wrap every Ktor suspend call in `Dispatchers.IO` by default.

## Presentation and navigation

- Follow the existing MVI-style shape: immutable `*State`, sealed `*Action`, sealed `*Event`, and a ViewModel `onAction()` entry point. State/action/event definitions may share the ViewModel file, as in `HomeViewModel.kt`.
- Expose read-only state via `asStateFlow()` and update through `_state.update { it.copy(...) }`. Use the existing buffered `Channel`/`receiveAsFlow()` pattern for one-shot events such as navigation and messages.
- Screen entry composables obtain their ViewModel with `koinViewModel()` and collect state with `collectAsStateWithLifecycle()`. Pass state and callbacks into reusable UI; keep ViewModels out of leaf components and previews.
- Existing screen entry points are named `*Screen`; do not impose a project-wide `*Root` rename. Keep navigation and other side effects in the screen integration boundary, not in ordinary composition or domain code.
- Decide which user-visible state must survive configuration changes and process death. Preserve the existing Navigation3 back-stack and saveable-entry behavior. Use `rememberSaveable` for appropriate local UI state or `SavedStateHandle` for ViewModel-owned restoration when needed; a ViewModel alone does not survive process death. Save small inputs/identifiers and reload content rather than saving large payloads or replaying one-shot events.
- The app uses Navigation3, with [navigation integration](app/src/main/java/com/arrazyfathan/kbbi/navigation/). Follow its route/key and entry patterns; feature screens expose navigation callbacks rather than owning the app graph.
- Reuse `KBBITheme`, Material 3 theme values, existing typography/components, semantic haptics, `UiText`, and `DataError.asUiText()`. Add user-facing strings to the existing English/Indonesian resources instead of hardcoding text. Shared resource imports commonly use `com.arrazyfathan.kbbi.core.R`.
- Keep full-screen feature app bars consistent with Proverb and Settings: use a `MediumTopAppBar` over the secondary-to-primary vertical gradient, the shared back icon and localized accessibility label, a Metropolis title that shrinks from 24sp to 20sp on collapse, an Inter subtitle while expanded, and `exitUntilCollapsedScrollBehavior` connected through the screen's nested scroll.
- Verify new strings in both English and Indonesian, including accessibility labels, plural forms where applicable, and matching format placeholders. Use locale-aware formatting for displayed numbers/dates and check that translated text fits the affected UI.
- Preserve edge-to-edge and IME inset handling, accessibility semantics, and stable lazy-list keys. Pair registered listeners/resources and app-wide loading ownership with disposal cleanup.

## Dependency injection and configuration

- Use Koin constructor injection and existing `singleOf`, `factoryOf`, `viewModelOf`, and `bind<Contract>()` conventions. Use provider lambdas when context or explicit construction is necessary.
- Register home data dependencies in `HomeDataModule.kt` (`repositoryModule`/`databaseModule`) and Word Study dependencies in `WordStudyDataModule.kt`/`WordStudyPresentationModule.kt`. Follow the equivalent data/presentation modules for other features.
- App-level use cases and most ViewModels are registered in [AppModule.kt](app/src/main/java/com/arrazyfathan/kbbi/di/AppModule.kt). [BaseApplication](app/src/main/java/com/arrazyfathan/kbbi/BaseApplication.kt) assembles modules. Inspect existing registration before creating a new module or duplicate binding.
- Declare dependencies in the module that uses them; keep versions and aliases in `gradle/libs.versions.toml`. Prefer `implementation` unless consumers require an exported API. Tests can need their own direct `testImplementation` dependencies.
- Use the Gradle wrapper. CI and JVM compilation use JDK 17; consult current Gradle files and the version catalog for SDK/library versions instead of copying stale version numbers.
- `KBBI_BASE_URL` is required. Resolution order is Gradle property, environment variable, then `local.properties`. Respect existing configuration; do not hardcode a developer's server or filesystem path.
- Default local verification to `developmentDebug`. Production release packaging requires configured signing credentials. Do not change version properties, signing, or distribution settings as part of ordinary feature work.

## Observability, logging, and privacy

- Use `AppLogger` and injected observability contracts rather than direct Firebase calls in feature code. Preserve collection gates and no-op reporter behavior.
- Extend the typed `AnalyticsEvent` model and existing enums for new analytics. Emit events at user-action or operation-result boundaries, and screen views at the navigation boundary. Avoid duplicate events from recomposition, repeated collection, or reporting the same action in multiple layers.
- Preserve reporting preferences: crash reporting defaults to enabled; analytics and performance monitoring default to disabled. Performance monitoring additionally requires the `production` flavor and `release` build type. Feature code must not bypass these gates or enable collection itself.
- Use `CrashReporter.recordNonFatal()` with `CrashOperation` and allowlisted, sanitized context for explicit failure reporting. `AppLogger` error calls with a throwable can already forward a non-fatal report through the installed remote sink; avoid reporting the same failure again explicitly. Keep expected validation errors and coroutine cancellation out of crash reports.
- Reuse shared HTTP request tracing rather than adding another trace around each feature request. When adding endpoints containing user content in paths or query parameters, verify URL sanitization and add regression tests for those routes. Preserve trace cleanup on success, failure, and cancellation.
- Keep search terms, definitions, translations, and dictionary visitor IDs out of analytics and diagnostic context. Reuse existing URL sanitization for network performance traces.
- Use fake reporters to assert feature events and no-op reporters when reporting is irrelevant to a test. Verify new event names/parameters, URL sanitization, and changed collection gates without contacting Firebase; run `:core:observability:testDebugUnitTest` when changing that module.
- Never commit local properties, signing credentials, service-account files, or other secrets, and do not print them in command output.

## Testing and verification

- Follow the module's existing JUnit 4 (`org.junit`) and assertion conventions. Do not introduce JUnit 5, another assertion library, or a test framework migration just to add a test.
- Use small fake domain repositories for use cases/ViewModels and Ktor `MockEngine` for HTTP contracts. Test observable behavior: normalization, result propagation, response mapping/order, empty results, errors, cache behavior, and cancellation where relevant.
- `HttpClientFactory` currently requires both `Json` and a `NetworkPerformanceReporter`. Test clients can pass `NoOpNetworkPerformanceReporter` with a direct test dependency on `:core:observability`. Close clients created by new tests.
- Run focused checks first. JVM domain modules use `test`; Android feature libraries use `testDebugUnitTest`; app tasks include the flavor. For home data/domain changes:

  ```sh
  ./gradlew --no-daemon :feature:home:domain:test :feature:home:data:testDebugUnitTest :app:compileDevelopmentDebugKotlin --console=plain
  ```

- For app-level integration changes, use the CI validation tasks as appropriate:

  ```sh
  ./gradlew --no-daemon testDevelopmentDebugUnitTest lintDevelopmentDebug assembleDevelopmentDebug --stacktrace
  ```

- App tests do not replace focused feature tests. Ktlint/Detekt are currently configured in `:app`; do not assume a root task covers all feature sources. Follow `.editorconfig`: four spaces, LF, final newline, 120-character Kotlin line limit, and trailing commas.
- App compilation checks types but does not prove Koin can resolve the runtime dependency graph. For DI changes, add or run a resolution test, or launch the app and exercise the affected dependency path when practical. Report any unverified runtime wiring explicitly.
- Check `git diff --check` and review the final diff. For documentation-only edits, verify referenced paths/commands; a full Android build is normally unnecessary.
- If a test exposes an existing failure, investigate and report it. Do not remove a failing test or claim coverage elsewhere without verifying that coverage. Distinguish compilation, unit tests, device checks, and live API verification in the handoff.

## Feature completion checklist

Before handing off a feature, verify the applicable items within the requested scope:

- Domain models, repository contracts, and use cases describe the intended behavior.
- DTOs/entities and mappers preserve the API or persistence contract.
- Repository and data-source implementations follow the intended error and cache policies.
- DI bindings are registered and direct constructor usages, including tests, are updated.
- Requested consumers are wired; explicitly deferred presentation work stays deferred.
- When changing shared word behavior or navigation, check affected widgets, reminders, launcher shortcuts, deep links, and external share/process-text intents. Verify the relevant entry points, including cold-start routing when affected, rather than relying only on in-app screen navigation.
- Check whether the change needs analytics or diagnostics. When adding reporting, verify collection preferences, privacy, and duplicate-reporting behavior.
- Relevant tests and checks pass, and the handoff identifies any failures or verification gaps.

## Useful implementation references

- Observability: [reporting contracts](core/observability/src/main/java/com/arrazyfathan/kbbi/core/observability/ReportingContracts.kt), [typed analytics events](core/observability/src/main/java/com/arrazyfathan/kbbi/core/observability/AnalyticsEvent.kt), [Firebase adapters and sanitization](core/observability/src/main/java/com/arrazyfathan/kbbi/core/observability/FirebaseReporters.kt), [collection preferences](core/observability/src/main/java/com/arrazyfathan/kbbi/core/observability/ReportingPreferencesDataStore.kt), and [observability tests](core/observability/src/test/java/com/arrazyfathan/kbbi/core/observability/).
- Word data flow: [WordRepository](feature/home/data/src/main/java/com/arrazyfathan/kbbi/feature/home/data/WordRepository.kt), [WordRemoteDataSource](feature/home/data/src/main/java/com/arrazyfathan/kbbi/feature/home/data/source/remote/WordRemoteDataSource.kt), and [WordMappers](feature/home/data/src/main/java/com/arrazyfathan/kbbi/feature/home/data/mapper/WordMappers.kt).
- Word Study data flow: [GenerateWordStudyUseCase](feature/wordstudy/domain/src/main/java/com/arrazyfathan/kbbi/feature/wordstudy/domain/usecase/GenerateWordStudyUseCase.kt), [NetworkWordStudyRepository](feature/wordstudy/data/src/main/java/com/arrazyfathan/kbbi/feature/wordstudy/data/NetworkWordStudyRepository.kt), and [AiSettingsScreen](feature/wordstudy/presentation/src/main/java/com/arrazyfathan/kbbi/feature/wordstudy/presentation/ai/AiSettingsScreen.kt).
- Small network feature: [GetTopWordsUseCase](feature/home/domain/src/main/java/com/arrazyfathan/kbbi/feature/home/domain/usecase/GetTopWordsUseCase.kt), [TopWordsRepository](feature/home/domain/src/main/java/com/arrazyfathan/kbbi/feature/home/domain/repository/TopWordsRepository.kt), and [TopWordsRemoteDataSource](feature/home/data/src/main/java/com/arrazyfathan/kbbi/feature/home/data/source/remote/TopWordsRemoteDataSource.kt).
- Network feature tests: [GetTopWordsUseCaseTest](feature/home/domain/src/test/java/com/arrazyfathan/kbbi/feature/home/domain/usecase/GetTopWordsUseCaseTest.kt) and [TopWordsRemoteDataSourceTest](feature/home/data/src/test/java/com/arrazyfathan/kbbi/feature/home/data/source/remote/TopWordsRemoteDataSourceTest.kt).
- Paginated/cache-backed feature: [NetworkProverbRepository](feature/proverb/data/src/main/java/com/arrazyfathan/kbbi/feature/proverb/data/NetworkProverbRepository.kt), [ProverbPagingSource](feature/proverb/data/src/main/java/com/arrazyfathan/kbbi/feature/proverb/data/ProverbPagingSource.kt), and [ProverbRemoteDataSource](feature/proverb/data/src/main/java/com/arrazyfathan/kbbi/feature/proverb/data/source/remote/ProverbRemoteDataSource.kt).
- Screen state and actions: [HomeViewModel](feature/home/presentation/src/main/java/com/arrazyfathan/kbbi/feature/home/presentation/home/HomeViewModel.kt) and [HomeScreen](feature/home/presentation/src/main/java/com/arrazyfathan/kbbi/feature/home/presentation/home/HomeScreen.kt); preferences: [settings modules](feature/settings/).
- Build and release source of truth: [settings.gradle.kts](settings.gradle.kts), module Gradle files, [version catalog](gradle/libs.versions.toml), and [Android CI](.github/workflows/android.yml).
