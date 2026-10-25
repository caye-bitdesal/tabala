# AGENTS.md

Guidance for AI coding agents working on **Tabala** (Muixeranga d'Elx). This file is the source of truth for agent behavior in this repo. If other docs or prompts (e.g. `.github/scripts/ai_review.py`, BitDesal skill templates) disagree, follow **AGENTS.md**.

## Project overview

Tabala is a **Kotlin Multiplatform** monorepo: shared domain models, a **Ktor** backend on the VPS, and **Compose Multiplatform** clients (Android, Desktop JVM, Web JS/Wasm). Product scope covers colla operations — members, events, areas, contracts, inventory, figures, documents, attendance — aligned with the backend data model and API contract.

Public web (Astro) and the management webapp are separate deployables; they talk to `api.<domain>` with Firebase ID tokens where required.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin (idiomatic) |
| UI | Compose Multiplatform + Material 3 in `:app:shared` |
| Architecture | MVVM + repositories; domain/DTOs in `:core` |
| DI | **Koin** (`koin-core`, `koin-compose`, platform modules) |
| Client networking | Ktor Client + kotlinx.serialization (JSON) |
| Server | Ktor (JVM 21), Flyway migrations, REST JSON API |
| Persistence | **SQLite** — authoritative on **`:server`** (SQLDelight or JDBC + Flyway schema); optional **SQLDelight cache** on Android/JVM clients when offline/read performance is needed |
| Auth | **Firebase Authentication** (Google + email/password); backend verifies ID tokens, stores UID + role — **no passwords on server** |
| Reactivity | Coroutines, `StateFlow` / `SharedFlow`, ViewModels in `commonMain` where possible |
| Serialization | kotlinx.serialization; contract per `backend/data-model/data-model.json` (IDs, ISO 8601 dates, `GeoPoint`) |

### Banned / do not introduce

- **Hilt, Dagger, Metro** — use Koin only.
- **Retrofit** on new code — prefer Ktor Client in shared code.
- **Room** — not multiplatform; use SQLDelight for SQLite on KMP/JVM/Android.
- **XML layouts** for screens — UI is Compose. XML for Android manifest/resources only.
- **Navigation 3** unless explicitly requested — use whatever navigation the project adopts in `commonMain` consistently.
- Storing **passwords** or long-lived Firebase secrets in the repo or server DB.

## Repository layout

```
core/                 # @Serializable models, shared validation, no UI
server/               # Ktor app, SQLite, Flyway, Firebase token verification, file storage
app/shared/           # Compose UI, ViewModels, repositories, Koin modules (commonMain + targets)
app/androidApp/       # Android entry + Koin start
app/desktopApp/       # JVM desktop entry
app/webApp/           # JS/Wasm browser entry
specs/                # Feature specs (read before implementing a feature)
```

Gradle modules: `:core`, `:server`, `:app:shared`, `:app:androidApp`, `:app:desktopApp`, `:app:webApp`.

## Backend alignment

- **Source of truth** for colla data: server SQLite + uploaded files under `files/`.
- **Roles:** `member`, `technical`, `board`, `admin` — enforce on server; client reflects forbidden actions in UI.
- **Public vs private:** respect RGPD — no public profile without `consent.publicProfile`; minors never public; do not expose internal notes or amounts on public routes.
- **Relations:** persist one side of each relation; derive the other (see backend stack doc).
- **Deploy:** fat JAR to VPS only; do not assume Gradle runs on the 2 GB server.

## Dependency injection (Koin)

- Define modules in `:app:shared` (`commonMain`) and platform `*Main` for expect/actual bindings (e.g. database driver, Firebase on Android).
- Start Koin in each app module's `Application` / `main()` — not in composable bodies.
- ViewModels: `koinViewModel()` or constructor injection via Koin; pass interfaces, not platform singletons.
- Tests: `startKoin { modules(testModule) }` / `stopKoin()` or Koin test APIs; use fakes for repositories and `HttpClient`.

## UI conventions (Compose Multiplatform)

- **Unidirectional flow:** UI reads state, sends events to ViewModel.
- Prefer **`collectAsStateWithLifecycle()`** on Android; on other targets use `collectAsState()` or lifecycle-compose where available.
- Correct side-effects: `LaunchedEffect`, `DisposableEffect` — no navigation as accidental composition side-effects.
- **File-level** `const val` and small private helpers **after imports**, before types (same habit as Weel/picalandroid).
- Previews where the target supports them; keep web/desktop-specific UI in the right source set.

## ViewModels and state

- One `*UiState` per non-trivial screen; expose `StateFlow`; mutate with `MutableStateFlow.update { }` in `viewModelScope`.
- Do not add manual equality guards before `update` — `update` already skips equal values.
- Keep API mapping and role checks in repositories or domain layer, not scattered in composables.

## Data layer

- DTOs and enums live in `:core` (English property names; Valencian labels in `enumLabels` when present in the contract).
- Repositories: interface in `commonMain`, implementation using Ktor + optional SQLDelight cache.
- Server: route → service → repository → SQLite; validate input; map errors to consistent HTTP status + JSON body.
- **Web (JS/Wasm):** no native SQLite — use in-memory cache or remote-only unless an explicit storage strategy is added.

## Specs-driven development

When implementing a feature, read **`specs/<feature>/`** (and backend specs if the flow is API-heavy) before coding. If spec and code conflict, ask the user.

## Testing

| Change type | Expected tests |
|-------------|----------------|
| ViewModel / repository / `:core` logic | `commonTest` or `jvmTest` in the owning module |
| Server routes / auth / SQL | `:server:test` |
| Android-specific wiring | `androidHostTest` / device tests when needed |
| Compose behavior | JVM/Android UI tests when interaction is non-trivial |

Run locally:

```bash
./gradlew :server:test :app:shared:jvmTest
```

Android unit tests when touching `androidMain`-only code: `./gradlew :app:androidApp:testDebugUnitTest` (when configured).

Do not add trivial wiring-only tests.

## Code quality priorities

1. **Bugs** — logic, nullability, concurrency, auth/role gaps, SQL/API mismatches.
2. **Platform correctness** — KMP source sets, Koin scopes, Ktor/Firebase/SQLDelight/Compose patterns.
3. **Understandability** — naming, single responsibility, constants over magic numbers.
4. **Reusability** — share logic in `:core` or `commonMain`; avoid duplicating DTO mapping.

**Simplicity:** one pass with a derived boolean beats two redundant filters unless order or draw semantics require otherwise.

## Agent workflow

- **Minimize scope** — smallest correct diff.
- **Match existing patterns** in the module you touch.
- **Ask** when product, RGPD, or schema choices are unclear.
- **Do not commit or push** unless the user asks.
- **Do not edit** CI/review scripts unless the task requires it.
- Commit prefixes when asked: `feat:`, `fix:`, `test:`, `spec:`, `care:`.

## References

- Backend stack and infra (vault): Muixeranga `backend-stack.md`, `infraestructura-servidor.md`
- Data model: `backend/data-model/data-model.json` (when present in repo or linked docs)
- BitDesal skills: `.cursor/skills/bitdesal-*` (KMP-aware prompts)
