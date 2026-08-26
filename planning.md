# Architecture evolution notes

This document records the current architecture and the most useful follow-up improvements. The earlier single-module migration plan is complete and has been replaced by the multi-module structure described here.

## Current state

- `:app` owns startup, root Navigation3 composition, external-intent routing, application-wide UI coordination, and Android platform adapters.
- Shared concerns are separated into `:core:*` modules for domain primitives, networking, logging, dependency injection, app updates, presentation utilities, design system, and platform helpers.
- Features are grouped by product capability and split into presentation, domain, and data modules where the capability owns meaningful business or persistence logic.
- Presentation uses immutable state, actions, ViewModels, and one-shot events.
- Domain modules expose focused repository interfaces and use cases.
- Data modules contain Room, Ktor, DataStore, asset, DTO, and mapper implementations.
- Koin modules are assembled by the application.

## Dependency rules

```text
presentation -> own domain + core presentation/domain
data         -> own domain + core data/domain/logging
domain       -> core domain only
app          -> feature and core modules for composition
```

Feature modules should not depend directly on another feature's presentation or data implementation. Shared cross-feature concepts should move to an appropriate `:core` module when they have more than one owner.

## Completed modernization

- [x] Multi-module feature and core structure
- [x] Focused word-search, bookmark, history, catalog, translation, and proverb contracts
- [x] Ktor Client and kotlinx.serialization networking
- [x] Shared typed `AppResult` and `DataError`
- [x] Shared `UiText`, alert, loading, and design-system infrastructure
- [x] Room-backed word and proverb caches
- [x] DataStore settings and WorkManager reminders
- [x] Navigation3 root graph with feature-owned routes
- [x] External intent and deep-link routing at the app boundary
- [x] App-update module backed by GitHub Releases
- [x] Unit, Android UI, Room, remote-source, and architecture-adjacent tests

## Recommended follow-up work

### Move global preferences out of feature ownership

`UiPreferences` currently lives under `:feature:settings:domain`, while the application shell consumes it to gate haptics across every feature. If more app-wide preferences are added, move the preference model and repository contract to `:core:domain` and the DataStore implementation to `:core:data`. Keep the app-shell ViewModel in `:app`.

### Reduce app-level feature knowledge

The app module currently registers several feature ViewModels and use cases. Prefer feature-owned Koin modules so `:app` only imports and combines module lists. This makes feature boundaries easier to test and limits composition-root churn.

### Clarify shared word ownership

Bookmark, detail, and words presentation reuse models and use cases currently owned by `:feature:home:domain`. If these workflows continue to grow independently, extract dictionary-wide contracts into a neutral domain module such as `:core:dictionary-domain` rather than introducing feature-to-feature dependencies.

### Strengthen module enforcement

- Add dependency-analysis checks to CI.
- Add module-level Detekt/Ktlint coverage consistently.
- Define convention plugins for Android library, Compose feature, pure Kotlin domain, Koin, Room, and serialization configuration.
- Keep generated sources and build outputs excluded from standalone formatting tools.

### Improve test layers

- Add tests for app-wide haptic preference collection and gating.
- Add WorkManager scheduler/worker integration tests.
- Add deep-link tests for proverb and bookmark launch requests.
- Add screenshot tests for key Compose screens and dark/theme variants when theming expands.

## Decision guide

- Keep code in a feature when that feature is its only owner.
- Move code to `:core:domain` when multiple features share a business concept.
- Move reusable UI behavior to `:core:presentation:ui` and visual primitives to the design system.
- Keep Android entry-point parsing, root navigation, and platform adapter wiring in `:app`.
- Avoid creating a new module for a single trivial class; use an existing cohesive module until the concern has a meaningful API surface.
