## ADDED Requirements

### Requirement: Display adaptive title in top app bar
The system SHALL display a `TopAppBar` at the top of the app shell. The title SHALL show the current user's group name if the user belongs to a group, or "Tabala" if they do not belong to any group.

#### Scenario: User does not belong to a group
- **WHEN** the app is launched and no group membership exists
- **THEN** the top app bar title SHALL display "Tabala"

#### Scenario: User belongs to a group
- **WHEN** the app is launched and the user belongs to a group (e.g., "Muixeranga d'Elx")
- **THEN** the top app bar title SHALL display the group's name

### Requirement: Display group logo in top app bar
The system SHALL display the group's logo on the end (right) side of the top app bar if the user belongs to a group. If the user does not belong to a group, no logo SHALL be shown.

#### Scenario: User belongs to a group with a logo
- **WHEN** the user belongs to a group that has a logo asset
- **THEN** the top app bar SHALL display the group logo on the right side

#### Scenario: User does not belong to a group
- **WHEN** no group membership exists
- **THEN** no logo or action icon SHALL be shown on the right side of the top app bar

### Requirement: Back navigation icon at second-level screens
The system SHALL display a back arrow navigation icon in the top app bar when the user is navigated to a second-level screen (i.e., `NavBackStack.size > 1`). At top-level screens, no navigation icon SHALL be shown.

#### Scenario: User is at a top-level screen
- **WHEN** the navigation back stack contains exactly one entry
- **THEN** no navigation icon SHALL be displayed in the top app bar

#### Scenario: User navigates to a second-level screen
- **WHEN** the navigation back stack contains more than one entry
- **THEN** the top app bar SHALL display a back arrow icon on the start (left) side

#### Scenario: User taps the back arrow
- **WHEN** the user taps the back arrow in the top app bar
- **THEN** the system SHALL navigate back by removing the last entry from the back stack
