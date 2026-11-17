# Tabala

Kotlin Multiplatform monorepo for **Muixeranga d'Elx** colla operations: shared domain models, a Ktor API on the VPS, and Compose Multiplatform clients (Android, desktop JVM, web JS/Wasm). The public Astro site and other management deployables live outside this repository; they talk to `api.<domain>` with Firebase ID tokens where required.

**Current state (early scaffold):** the Gradle layout and targets are in place; product features are mostly ahead of the code.

| Area | Status |
|------|--------|
| **`:core`** | Shared KMP library wired into server and apps; only placeholder helpers today — DTOs aligned with the backend data model are still to land here. |
| **`:server`** | Ktor on Netty (`GET /` hello), SQLite with WAL + foreign keys, Flyway on startup. **V1** SQLDelight migration defines the full Muixeranga schema (seed data includes position types). REST, Firebase auth, and file storage are not implemented yet. |
| **`:app:shared`** | Compose Material 3 template screen (`App` + greeting); no navigation, ViewModels, or API client yet. |
| **Clients** | `:app:androidApp`, `:app:desktopApp`, `:app:webApp` (JS and Wasm browser targets) bootstrap and embed shared UI. |

For module boundaries and deployment intent, see [docs/module-schema.md](./docs/module-schema.md). Agent and contribution conventions: [AGENTS.md](./AGENTS.md).

### Stack

Kotlin **2.4**, Compose Multiplatform, **Koin** (DI as features grow), **Ktor** + kotlinx.serialization on the server, **SQLite** (SQLDelight queries, Flyway migrations), **Firebase Auth** on clients (token verification on server — planned).

### Running the apps

Use the run configurations in your IDE, or:

- Android: `./gradlew :app:androidApp:assembleDebug`
- Desktop: `./gradlew :app:desktopApp:run` (hot reload: `:app:desktopApp:hotRun --auto`)
- Server: `./gradlew :server:run` — listens on `8080`; default DB `data/muixeranga.db` (override with `DB_PATH`)
- Web:
  - Wasm (preferred): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
  - JS: `./gradlew :app:webApp:jsBrowserDevelopmentRun`

`kotlin-js-store/` is gitignored; Yarn lockfiles are generated locally by the Kotlin/JS Gradle plugin when you build web targets.

### Running tests

- Server: `./gradlew :server:test`
- Shared (desktop/JVM): `./gradlew :app:shared:jvmTest`
- Android host tests: `./gradlew :app:shared:testAndroidHostTest`
- Web: `./gradlew :app:shared:jsTest` or `:app:shared:wasmJsTest`

### Learn more

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/)