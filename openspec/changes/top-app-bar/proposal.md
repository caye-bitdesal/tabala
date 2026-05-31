## Assumptions to Verify

1. Group membership data is mocked (hardcoded stub) for now; real auth/API integration is out of scope.
2. Group logos are local drawable assets; remote image loading (Coil/Glide) is not required.
3. Navigation depth is determined by `NavBackStack.size > 1` — no extra state management needed.
4. Koin is the DI framework; Hilt/Dagger are rejected.
5. The `MainViewModel` lives at the activity level and is shared across all screens via `koinViewModel()`.

---

## Why

The app currently has no top app bar, so users have no persistent header showing the app or group context, and no standard back-navigation affordance on second-level screens. Adding a Material 3 `TopAppBar` provides proper navigation hierarchy and group-membership branding.

## What Changes

- Add `TabalaTopAppBar` composable: Material 3 `TopAppBar` with adaptive title, optional navigation icon, and optional group logo.
- Introduce a minimal data/domain layer: `UserGroup` model, `UserGroupRepository`, stub implementation, and `GetCurrentUserGroupUseCase`.
- Add `MainViewModel` that exposes `StateFlow<UserGroup?>` to the UI.
- Wire Koin DI: new `AppModule`, new `TabalaApplication` class.
- Integrate the app bar into the existing `Scaffold` in `TabalaApp` / `MainActivity`.

## Capabilities

### New Capabilities

- `top-app-bar`: Displays the app bar with adaptive title (group name or "Tabala"), optional back arrow at depth > 1, and optional group logo on the right.

### Modified Capabilities

_(none — no existing spec-level behavior changes)_

## Impact

- **New files:** `TabalaApplication.kt`, `AppModule.kt`, `UserGroup.kt`, `UserGroupRepository.kt`, `UserGroupRepositoryImpl.kt`, `GetCurrentUserGroupUseCase.kt`, `MainViewModel.kt`, `TabalaTopAppBar.kt`, `res/drawable/logo_muixeranga_elx.png`
- **Modified files:** `MainActivity.kt`, `AndroidManifest.xml`, `strings.xml`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
- **Dependencies added:** `lifecycle-viewmodel-compose`, `koin-android`, `koin-androidx-compose`

## Non-goals / Discarded

- Real group membership from a backend API (Retrofit/OkHttp) — deferred to a future change.
- Remote logo loading (Coil, Glide, Picasso) — local drawables only.
- Deep-link handling or dynamic navigation graph changes.
- XML layouts — all UI is Compose.
- Hilt/Dagger — Koin only.
