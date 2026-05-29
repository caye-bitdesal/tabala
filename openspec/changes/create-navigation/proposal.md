## Assumptions to Verify

- Top-level destinations are **Inici**, **Muixeranges**, **Figures**, and **Perfil** with custom tab icons.
- Tab switches **replace** the back stack root (no nested stacks per tab in v1).
- System back on a tab exits the app when the stack has one entry (standard single-activity behavior).
- **No deep links**, auth-gated graphs, or list-detail scenes in this change.
- Koin is **not** required yet; navigation state lives in the root composable until feature modules arrive.

## Business value and technical impact

**Value:** Establishes a standards-compliant navigation foundation aligned with Tabala’s four main sections and branded tab icons.

**Impact:** Navigation 3 back stack and `NavDisplay` wire four placeholder screens with Catalan labels and drawable icons (`ic_inici`, `ic_muixeranges`, `ic_figures`, `ic_perfil`).

## Why

The app needs domain-specific top-level sections (castells-related: home, human towers, figures, profile) rather than generic Home/Favorites/Profile tabs. Navigation 3 provides typed routes and back-stack semantics for future feature work.

## What Changes

- Four `NavKey` destinations: Inici, Muixeranges, Figures, Perfil.
- Navigation suite labels and icons from `strings.xml` and attached PNG drawables.
- Placeholder screen per destination.
- Default start destination: Inici.

## Capabilities

### New Capabilities

- `app-navigation`: Top-level tab navigation for Inici, Muixeranges, Figures, and Perfil.

### Modified Capabilities

- (none)

## Impact

- **Code:** `MainActivity.kt`, `navigation/*`, `ui/inici|muixeranges|figures|perfil`, `res/drawable/ic_*.png`, `values/strings.xml`.
- **Non-breaking** for navigation mechanics; tab set and labels are product-specific.

## Non-goals / Discarded

- XML navigation graphs, Navigation 2 `NavHost`, deep links, list-detail adaptive scenes.
- Room, Retrofit, Koin, production feature UI beyond placeholders.
- Legacy English tabs (Home, Favorites, Profile) and Material default icons.
