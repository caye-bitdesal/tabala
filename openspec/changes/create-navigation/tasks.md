## 1. App navigation — Data layer

- [x] 1.1 **(Data)** N/A — No Room entities or local persistence for navigation; document skip in change notes.

## 2. App navigation — Network layer

- [x] 2.1 **(Domain/Data)** N/A — No API contracts or DTOs for navigation.

## 3. App navigation — UI layer

- [x] 3.1 **(UI)** Add Navigation 3 and Kotlin Serialization dependencies in `libs.versions.toml` and `app/build.gradle.kts`.
- [x] 3.2 **(UI)** Create `NavKey` types and `tabalaEntryProvider` under `navigation/`.
- [x] 3.3 **(UI)** Add placeholder screens for Inici, Muixeranges, Figures, and Perfil.
- [x] 3.4 **(UI)** Refactor `TabalaApp` to use `rememberNavBackStack`, `NavDisplay`, and wire `NavigationSuiteScaffold` to the back stack.
