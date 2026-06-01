## Context

The app currently has no top app bar. All four screens (Inici, Muixeranges, Figures, Perfil) are stateless placeholders using a `NavigationSuiteScaffold` inside a plain `Scaffold` with no `topBar` slot. There is no data or domain layer, no ViewModel, and no DI framework wired up yet.

The feature requires reading group-membership state from a repository and exposing it reactively to the UI. The plan uses a stub implementation to unblock UI development before real auth exists.

## Goals / Non-Goals

**Goals:**
- Add a `TopAppBar` slot to the existing `Scaffold` in `TabalaApp`.
- Show adaptive title (group name or "Tabala") driven by a ViewModel.
- Show a back arrow at `NavBackStack.size > 1`, hide it at top-level depth.
- Show group logo (local drawable) on the right when membership exists.
- Wire Koin DI so the ViewModel can be injected with `koinViewModel()`.

**Non-Goals:**
- Real API call for group membership — stub only.
- Remote image loading for logos.
- Handling multiple groups per user.
- Deep-link triggered navigation changes.

## Decisions

### 1. Koin as the DI framework
Koin is mandated by the project constitution. Hilt/Dagger are rejected. `startKoin { modules(appModule) }` is called in a custom `TabalaApplication`.

### 2. `MainViewModel` at activity scope
The app has a single activity (`MainActivity`). The `MainViewModel` is scoped to the activity via `koinViewModel()` inside the `TabalaApp` composable. This ensures the top app bar state survives screen transitions without re-fetching.

**Alternative considered:** passing state as parameters down to each screen. Rejected — the app bar is at the app-shell level, not per-screen.

### 3. `StateFlow<UserGroup?>` with `stateIn`
The ViewModel collects from a cold `flow { emit(useCase()) }` and converts it to a `StateFlow` with `SharingStarted.WhileSubscribed(5_000)` and initial value `null`. While the flow is loading, the title shows "Tabala" (null fallback). This avoids a separate loading state enum for this simple case.

### 4. Back-button depth via `NavBackStack.size`
Navigation 3's `NavBackStack` is already available in `TabalaApp`. `showBackButton = backStack.size > 1` is computed directly in the composable, with `onBackClick = { backStack.removeLastOrNull() }`. No extra state management needed.

### 5. Local drawable for group logo
Logos are placed in `res/drawable/` as PNG assets. `UserGroup.logoResId: Int?` holds a `@DrawableRes` ID. Null means no logo is shown. Remote loading is deferred.

### 6. Package layout
New files follow the existing package `com.bitdesal.tabala`:

| Package | Contents |
|---|---|
| `domain.model` | `UserGroup` data class |
| `domain.repository` | `UserGroupRepository` interface |
| `domain.usecase` | `GetCurrentUserGroupUseCase` |
| `data.repository` | `UserGroupRepositoryImpl` (stub) |
| `di` | `AppModule`, `TabalaApplication` |
| `ui.main` | `MainViewModel` |
| `ui.components` | `TabalaTopAppBar` |

## Risks / Trade-offs

- **Stub data**: `UserGroupRepositoryImpl` always returns "Muixeranga d'Elx". This is intentional for now but must be replaced before shipping real group auth. → _Mitigation: clearly mark as `TODO` in code._
- **Single-activity scope**: If a multi-activity flow is ever added, the ViewModel scope assumption breaks. → _Mitigation: document the single-activity constraint._
- **Logo drawable coupling**: Using `@DrawableRes Int?` ties the domain model to Android resources. → _Mitigation: acceptable for a local-first v1; decouple via URL string in a future change._

## Migration Plan

1. Add Koin + lifecycle-viewmodel-compose dependencies.
2. Create data/domain/DI layers and wire `TabalaApplication`.
3. Register `TabalaApplication` in `AndroidManifest.xml`.
4. Add `TabalaTopAppBar` composable.
5. Update `MainActivity.kt` to inject `MainViewModel` and add `topBar` to `Scaffold`.
6. Add string resources and copy logo asset.
7. Build and verify with `./gradlew :app:assembleDebug`.

## Open Questions

_(none — all decisions resolved; real auth is explicitly deferred)_
