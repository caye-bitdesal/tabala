## 1. Data — Model & Repository

- [ ] 1.1 Create `domain/model/UserGroup.kt` data class with `id: String`, `name: String`, `@DrawableRes logoResId: Int?`
- [ ] 1.2 Create `domain/repository/UserGroupRepository.kt` interface with `getCurrentUserGroup(): UserGroup?`
- [ ] 1.3 Create `data/repository/UserGroupRepositoryImpl.kt` stub returning Muixeranga d'Elx with local logo asset

## 2. Domain — Use Case & DI

- [ ] 2.1 Create `domain/usecase/GetCurrentUserGroupUseCase.kt` thin operator-fun wrapper over `UserGroupRepository`
- [ ] 2.2 Create `di/AppModule.kt` Koin module: `single<UserGroupRepository>`, `factory { GetCurrentUserGroupUseCase(...) }`, `viewModel { MainViewModel(...) }`
- [ ] 2.3 Create `TabalaApplication.kt` `Application` subclass calling `startKoin { modules(appModule) }`

## 3. UI — ViewModel, Composable & Scaffold

- [ ] 3.1 Create `ui/main/MainViewModel.kt` exposing `StateFlow<UserGroup?>` via `stateIn(WhileSubscribed(5_000), null)`
- [ ] 3.2 Create `ui/components/TabalaTopAppBar.kt` M3 `TopAppBar` with conditional back arrow and conditional group logo
- [ ] 3.3 Update `MainActivity.kt`: inject `MainViewModel` with `koinViewModel()`, compute `showBackButton = backStack.size > 1`, add `topBar` slot to `Scaffold`

## 4. Resources & Manifest

- [ ] 4.1 Add `app_title` ("Tabala") and `action_back` to `strings.xml`
- [ ] 4.2 Copy `logo_muixeranga_elx.png` to `res/drawable/`
- [ ] 4.3 Register `TabalaApplication` in `AndroidManifest.xml` via `android:name`

## 5. Dependencies

- [ ] 5.1 Add `lifecycle-viewmodel-compose`, `koin-android`, `koin-androidx-compose` versions to `gradle/libs.versions.toml`
- [ ] 5.2 Add corresponding `implementation(...)` entries to `app/build.gradle.kts`
