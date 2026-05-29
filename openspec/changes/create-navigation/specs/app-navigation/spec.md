## ADDED Requirements

### Requirement: Navigate between top-level tabs

The app SHALL expose four top-level destinations—Inici, Muixeranges, Figures, and Perfil—and SHALL display the composable content associated with the active destination.

#### Scenario: User selects Inici tab

- **WHEN** the user taps the Inici item in the navigation suite
- **THEN** the back stack SHALL contain only the Inici destination and Inici content SHALL be visible

#### Scenario: User selects Muixeranges tab

- **WHEN** the user taps the Muixeranges item in the navigation suite
- **THEN** the back stack SHALL contain only the Muixeranges destination and Muixeranges content SHALL be visible

#### Scenario: User selects Figures tab

- **WHEN** the user taps the Figures item in the navigation suite
- **THEN** the back stack SHALL contain only the Figures destination and Figures content SHALL be visible

#### Scenario: User selects Perfil tab

- **WHEN** the user taps the Perfil item in the navigation suite
- **THEN** the back stack SHALL contain only the Perfil destination and Perfil content SHALL be visible

### Requirement: Tab labels and icons match product design

Each navigation suite item SHALL display the Catalan label (Inici, Muixeranges, Figures, Perfil) and SHALL use the corresponding branded icon drawable (`ic_inici`, `ic_muixeranges`, `ic_figures`, `ic_perfil`).

#### Scenario: Navigation bar displays branded items

- **WHEN** the navigation suite is visible
- **THEN** four items SHALL be shown with the correct labels and icon drawables for each section

### Requirement: Tab selection reflects back stack

The navigation suite SHALL mark the item selected whose destination matches the top entry of the navigation back stack.

#### Scenario: Stack top is Muixeranges

- **WHEN** the back stack top is the Muixeranges destination
- **THEN** the Muixeranges navigation suite item SHALL appear selected

### Requirement: System back respects single-level stack

When the back stack contains a single top-level destination, the system back action SHALL not leave a stale intermediate destination within the app.

#### Scenario: Back on root destination

- **WHEN** the back stack has exactly one top-level entry and the user invokes system back
- **THEN** the activity SHALL delegate back to the system (finish or move task) without pushing another in-app destination

### Requirement: Navigation uses Navigation 3 APIs

In-app navigation SHALL be implemented with Navigation 3 (`NavKey`, back stack, `NavDisplay`, `entryProvider`) and SHALL NOT use XML navigation graphs or Navigation 2 `NavHost` as the primary mechanism.

#### Scenario: Implementation inspection

- **WHEN** reviewing the root navigation wiring
- **THEN** destination types SHALL be `NavKey` implementations and content SHALL be provided through an `entryProvider` consumed by `NavDisplay`

## Data Contract

Not applicable for this capability. Top-level navigation is entirely in-memory; no API or persistence payloads are defined.
