## Context

Tabala is a single-activity Compose app using Navigation 3 with `NavigationSuiteScaffold`. Top-level sections reflect the product: Inici, Muixeranges, Figures, and Perfil, each with a custom PNG tab icon.

## Goals / Non-Goals

**Goals:**

- Four `NavKey` destinations with Catalan labels in `strings.xml`.
- Branded icons: `ic_inici`, `ic_muixeranges`, `ic_figures`, `ic_perfil` (PNG drawables).
- `rememberNavBackStack(IniciNavKey)` as default; tab switch replaces stack root.
- Placeholder screens under `ui/inici`, `ui/muixeranges`, `ui/figures`, `ui/perfil`.

**Non-Goals:**

- Deep links, nested graphs per tab, ViewModel scoping add-on, adaptive list-detail scenes.
- Koin, Room, Retrofit, or production feature logic.

## Decisions

1. **Four domain tabs** — Replaces generic Home/Favorites/Profile with castells-oriented sections.
2. **String resources for labels** — Per project rules (XML for strings); icons remain drawables.
3. **PNG tab icons** — User-provided assets copied to `res/drawable/`; no vector tinting required.
4. **Package layout** — `navigation/` for keys; `ui/<section>/` per screen.
5. **Tab replace on select** — Same as v1 navigation: single entry per tab in back stack.

## Risks / Trade-offs

- **[Risk] Renamed `NavKey` types break saved back stack** → Acceptable during development; users may need fresh install after upgrade.
- **[Risk] PNG icons differ in size** — Use `Icon` without forced size; suite scales as needed.

## Migration Plan

1. Rename NavKeys and screens; add Figures destination.
2. Copy icon PNGs; update `strings.xml` and `TopLevelDestinations`.
3. Remove legacy `ui/home`, `ui/favorites`, `ui/profile` and unused vector icons if desired.

## Open Questions

- None blocking; icon color on unselected state follows Material `Icon` default (may tint non-red icons).
