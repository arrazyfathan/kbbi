# Architecture Fix Plan

## Scope

This plan executes the 1-7 review findings while keeping the project as a single `:app` module for now. The package boundaries are prepared so future Gradle modules can be introduced with less churn.

## Completed Fixes

- [x] Document current clean architecture scope and future modularization target.
- [x] Replace broad `IWordRepository` with focused domain contracts:
  - `WordSearchRepository`
  - `BookmarkRepository`
  - `SearchHistoryRepository`
  - `WordCatalogRepository`
- [x] Rename generic data source classes to source-specific names:
  - `RoomWordLocalDataSource`
  - `RetrofitWordRemoteDataSource`
  - `AssetWordCatalogRepository`
- [x] Move `entries.json` loading/parsing out of `WordListScreen` and into the data/domain flow.
- [x] Move mapper logic from `core.utils.DataMapper` to data-layer extension mappers.
- [x] Remove JSON route serialization from ViewModels; navigation owns route serialization.
- [x] Update stale tests from removed `Resource` / `WordInteractor` APIs to current use case and `AppResult` APIs.

## Future Modularization Preparation

When the app is ready for multi-module migration, split the current packages into:

- `:core:domain`
  - `core/domain/model`
  - `core/domain/repository`
  - `core/domain/usecase`
- `:core:data`
  - `core/data`
  - `core/data/source`
  - `core/data/mapper`
  - Room, Retrofit, asset-backed repository implementations
- `:core:presentation`
  - `presentation/common`
  - shared UI error mapping / loading controller
- `:feature:home:presentation`
- `:feature:words:presentation`
- `:feature:bookmarks:presentation`
- `:feature:detail:presentation`
- `:app`
  - `BaseApplication`
  - `MainActivity`
  - DI assembly
  - top-level navigation

Current single-module code should preserve these dependency directions:

- `presentation` depends on domain use cases and presentation-common only.
- `domain` depends on domain models/contracts only.
- `data` depends on domain contracts/models and platform/data frameworks.
- DI binds concrete data implementations to domain interfaces.

## Verification

Run after edits:

```bash
./gradlew --no-daemon testDevelopmentDebugUnitTest --console=plain
./gradlew --no-daemon ktlintCheck --console=plain
```
