# Intake: Home Screen UI

Status: Draft
Created: 2026-06-21
Artifact root: `documentation/features`

## Sources

| Type | Path / URL | Notes |
| --- | --- | --- |
| User request | Conversation, 2026-06-21 | Build the home screen from UI screenshots; UI only; no backend work; create branch first, draft PR at end, include PR babysitting as final workflow step. |
| Design | `screen-mockup-images/home-screen-no-sessions.png` | Target empty home state with left rail, top bar, centered hero empty state, primary action, shared library link, bottom feature hints, and favorites/help controls. |
| Design | `screen-mockup-images/home-screen-with-sessions.png` | Target populated home state with left rail, top actions, select-new-media pill, centered section title, session cards, and floating favorites action. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeScreen.kt` | Existing Compose entry point selects empty or populated home content from `HomeUiState`. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeEmptyState.kt` | Existing empty-state UI; primary implementation target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeSessionList.kt` | Existing populated-state UI and session cards; primary implementation target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/AppShell.kt` | Existing shell that wraps top bar, left rail, and home content. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtNavRail.kt` | Existing left navigation rail; target for visual matching. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtTopBar.kt` | Existing top bar; target for visual matching. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt` | Existing brand palette used by home UI. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtTypography.kt` | Existing Literata typography setup. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeSampleSessions.kt` | Existing sample session titles and timestamps match the populated mockup. |

## Product Goal

Deliver a polished Android Compose home screen that closely matches the supplied empty and populated home screen mockups while preserving current navigation and local UI behavior.

## UI Surfaces

- Home screen shell: left navigation rail, top brand/navigation/action bar, content area.
- Empty home state: centered media illustration, headline, supporting copy, primary session button, shared library link, bottom feature hints.
- Populated home state: select-new-media pill, section heading, session cards with thumbnails/actions, floating favorites button.
- Existing UI-only affordances: new session sheet, sync dialog, coming-soon callbacks, and watch navigation should continue to be triggered by existing handlers.

## Requirements

- Match the two supplied screenshots as closely as practical in Compose, using the existing project theme and components where possible.
- Keep work UI-only: do not add database work, API calls, authentication flows, or backend integration.
- Preserve existing state flow from `HomeViewModel` and existing callbacks in `HomeScreen.kt`.
- Keep both empty and populated home states represented.
- Build reusable UI components where useful for buttons, cards, thumbnails, rail items, and shared styling.
- Create workflow artifacts under `documentation/features`.
- Create branch before implementation; branch is `feature/home-screen-ui`.
- Prepare a draft PR at the end and include PR babysitting/monitoring as the final workflow step.

## Non-Goals

- No database, repository, API, network, authentication, or account work.
- No watch-session implementation beyond preserving existing navigation behavior.
- No real movie metadata or poster fetching.
- No changes to product data models unless needed solely for UI presentation and approved later.
- No merging of the draft PR by automation.

## Constraints

- Android app uses Kotlin and Jetpack Compose under `app/src/main/java/com/l8r2gether/app`.
- Existing UI pulls home state from `HomeViewModel`; implementation should avoid backend behavior changes.
- Screenshots are static visual references; exact source image assets for avatar/movie posters are not present in `screen-mockup-images`.
- Existing Literata Google Font setup should be retained unless implementation discovers a blocking issue.
- Android UI verification should use emulator evidence when practical, following `android-emulator-qa`.

## Risks

- Pixel-perfect parity may be limited by missing original poster/avatar assets and exact design tokens.
- Current shell and home components already diverge from the screenshots in spacing, top-bar composition, rail width, selected states, and card proportions; changes must stay coordinated across these files.
- Compose font availability through Google Fonts may affect local emulator rendering if Google Play services font provider is unavailable.
- Without backend changes, populated state depends on existing local/sample data paths; UI work should not invent data loading behavior.

## Open Questions

- [ ] Are exact avatar/poster bitmap assets available outside `screen-mockup-images`, or should implementation use Compose placeholders/gradients that visually approximate them? Owner: user. Blocking: No, unless exact asset parity is required.

## Suggested Next Step

Create one feature for the home screen visual refresh, review it against both mockups and the UI-only constraint, then request approval before task decomposition.
