# AGENTS.md

Guidance for AI coding agents working on **Tabala** (Muixeranga d'Elx). This file is the source of truth for agent behavior in this repo. If other docs or prompts (e.g. `.github/scripts/ai_review.py`, BitDesal skill templates) disagree, follow **AGENTS.md**.

## Project overview

Tabala is a **Kotlin Multiplatform** monorepo: shared domain models, a **Ktor** backend on the VPS, and **Compose Multiplatform** clients (Android, Desktop JVM, Web JS/Wasm). Product scope covers colla operations — members, events, areas, contracts, inventory, figures, documents, attendance — aligned with the backend data model and API contract.

Public web (Astro) and the management webapp are separate deployables; they talk to `api.<domain>` (Google Sign-In ID tokens exchanged for backend HS256 JWT + refresh sessions — **no Firebase**).

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin (idiomatic) |
| UI | Compose Multiplatform + Material 3 in `:app:shared` (v1 target: Web `wasmJs` via `:app:webApp`) |
| Architecture | MVVM + repositories; domain/DTOs, shared validation, i18n enum labels and `MuixerangaApi` (Ktor Client) in `:core` |
| DI | **Koin** (`koin-core`, `koin-compose`, `koin-ktor`, platform modules) |
| Client networking | Ktor Client + kotlinx.serialization (JSON) in `:core` |
| Server | Ktor 3.x (JVM 21), Flyway migrations, REST JSON API |
| Persistence | **SQLite** (WAL, `foreign_keys=ON`) — authoritative on **`:server`**: schema v0.2 in SQLDelight migrations (`server/src/main/sqldelight/migrations/V<n>__<name>.sqm`), exported to `db/migration/*.sql` and applied by **Flyway** at startup; queries via SQLDelight. Optional **SQLDelight cache** on Android/JVM clients in future phases |
| Auth | **Google Sign-In** (OpenID Connect, invite-only) + own backend sessions (HS256 JWT 15 min + rotating refresh token 30 d); **no Firebase, no passwords on server** |
| Reactivity | Coroutines, `StateFlow` / `SharedFlow`, ViewModels in `commonMain` where possible |
| Serialization | kotlinx.serialization; contract per `docs/especificacion-tecnica.md` v1.3 (model v0.2) and `docs/decisiones.md` |

### Banned / do not introduce

- **Firebase** — use Google Sign-In (OIDC) + own sessions only.
- **Exposed** — use SQLDelight + Flyway.
- **Hilt, Dagger, Metro** — use Koin only.
- **Retrofit** on new code — prefer Ktor Client in shared code (`:core`).
- **Room** — not multiplatform; use SQLDelight for SQLite on KMP/JVM/Android.
- **XML layouts** for screens — UI is Compose. XML for Android manifest/resources only.
- **Navigation 3** unless explicitly requested — use Navigation Compose 2 consistently in `commonMain`.
- Storing **passwords**, Google ID tokens, or raw refresh tokens in the repo or server DB.

## Repository layout

```
core/                 # @Serializable v0.2 DTOs, enums, i18n labels, shared validation, MuixerangaApi (Ktor Client), no UI
server/               # Ktor app, SQLite v0.2, Flyway, Google OIDC + JWT/session auth, file storage
app/shared/           # Compose UI, ViewModels, repositories, Koin modules (commonMain + targets)
app/androidApp/       # Android entry (out of v1 scope)
app/desktopApp/       # JVM desktop entry (out of v1 scope)
app/webApp/           # Web wasmJs browser entry (v1 management webapp)
docs/                 # especificacion-tecnica.md (product source of truth) + decisiones.md
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

- Define modules in `:app:shared` (`commonMain`) and platform `*Main` for expect/actual bindings (e.g. GIS DOM bridge, browser file download, URL sync on `wasmJsMain`).
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
2. **Platform correctness** — KMP source sets, Koin scopes, Ktor/Google OIDC/SQLDelight/Compose patterns.
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

- Product and technical spec: `docs/especificacion-tecnica.md` (v1.3)
- Implementation decisions: `docs/decisiones.md`
- BitDesal skills: `.cursor/skills/bitdesal-*` (KMP-aware prompts)
