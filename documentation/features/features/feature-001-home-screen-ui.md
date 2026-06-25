# Feature: feature-001-home-screen-ui Home Screen UI Refresh

Status: Draft
Parent intake: `documentation/features/intake/home-screen-ui-intake.md`

## Source References

| Source | Reference | Requirement / Design Detail |
| --- | --- | --- |
| User request | Conversation, 2026-06-21 | Build the home screen only; match screenshots closely; no backend/database/API/auth work; create branch at start, draft PR and PR babysitting at end. |
| Design | `screen-mockup-images/home-screen-no-sessions.png` | Empty state target: cream/blush background, pale left rail, serif brand, top navigation, centered large icon tile, two-heart accent, headline/body/action/link, bottom feature hints, favorites/help controls. |
| Design | `screen-mockup-images/home-screen-with-sessions.png` | Populated state target: compact top bar, select-new-media pill, bold centered title/subtitle, two white rounded session cards with thumbnails/actions/notes icon, bottom-right favorites FAB. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeScreen.kt` | Preserve current state branching and callbacks. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeEmptyState.kt` | Empty-state implementation target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeSessionList.kt` | Session-list implementation target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/AppShell.kt` | Shell layout target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtNavRail.kt` | Rail layout and item styling target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtTopBar.kt` | Top brand/nav/actions target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt` | Palette should be tuned or extended for mockup parity. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtTypography.kt` | Typography should continue using Literata for brand/headings. |

## User Outcome

Users see a home screen that feels like the provided LaterTogether mockups in both first-run/empty and returning-user/session-list states, while existing UI actions still open the same local sheets, dialogs, and navigation callbacks.

## Scope

### In Scope

- Visual refresh of the home shell, left rail, top bar, empty state, session list, session cards, action buttons, icon treatments, and floating favorites action.
- Reusable Compose components or helpers where they reduce duplication between home states.
- UI-only placeholder thumbnail treatments that approximate the screenshots without fetching external content.
- Existing string updates if needed for visual parity, while preserving current meaning.
- Compose previews or sample UI paths that help verify empty and populated states.
- Emulator or build verification after implementation when practical.
- Workflow artifacts through implementation, work review, draft PR, and PR monitoring.

### Out Of Scope

- Database changes, repository changes, API calls, auth, remote media search, account/profile implementation, and real avatar/poster loading.
- Changes to watch playback, moments, lounge, favorites, settings, or help behavior beyond existing callbacks.
- Merging the PR.

## UX / UI Notes

- Use a warm off-white background and pale blush left rail matching the screenshots.
- Left rail should show `Us`, selected Cinema tile, Moments, Lounge, bottom favorites circle, and Help.
- Empty state should be centered vertically with a soft glow behind the media tile and feature hints near the lower portion of the content area.
- Populated state should use a centered content column, with the select-media pill above the heading and session cards constrained to match the screenshot proportions.
- Cards should use large but controlled corner radii, white fill, very soft shadow/elevation, thumbnail block on the left, text/actions in the middle, and a notes icon at the right.
- Top-right action icons and avatar should visually resemble the mockups; if no avatar asset exists, use a UI-only placeholder.

## Dependencies

- Existing Compose Material 3 dependencies and icon set.
- Existing `HomeUiState` and `SessionListItemUi` data from `HomeViewModel`.
- Existing project font/provider setup.
- Android emulator availability for final QA evidence.
- Draft PR creation depends on local git state and GitHub access at integration time.

## Risks

- Missing exact bitmap assets may make poster/avatar parity approximate rather than exact.
- Over-adjusting theme tokens could affect screens outside home; prefer local or narrowly named tokens unless global theme changes are clearly intended.
- Rail/top-bar changes can affect all shell-wrapped screens, so implementation should verify watch stub or avoid behavior changes.
- The task is visual and subjective; screenshots and emulator captures should be used as acceptance evidence.

## Feature Acceptance Criteria

- [ ] Empty home state visually matches `home-screen-no-sessions.png` in structure, spacing, palette, typography hierarchy, and controls as closely as practical.
- [ ] Populated home state visually matches `home-screen-with-sessions.png` in structure, spacing, palette, typography hierarchy, session card layout, and floating action placement as closely as practical.
- [ ] Existing `HomeScreen.kt` state branching and callbacks remain intact.
- [ ] No backend, database, API, auth, or data-loading behavior is added.
- [ ] Reusable UI pieces are introduced where appropriate without broad unrelated refactors.
- [ ] Build or compile verification passes, or any failure is documented with cause.
- [ ] Visual verification evidence is captured when practical.
- [ ] Draft PR artifact and PR babysitting/monitoring artifact are created before final handoff.

## Candidate Tasks

- `task-001-home-shell-chrome`: Match the shell, rail, top bar, color tokens, typography sizing, and shared UI primitives.
- `task-002-home-empty-state`: Match the no-sessions home state.
- `task-003-home-session-list`: Match the populated session-list state and cards.
- `task-004-home-ui-verification`: Build, emulator/visual verification, work review, draft PR, and PR monitoring setup.

## Open Questions

- [ ] Should implementation use Compose-drawn placeholder thumbnails/avatar if exact assets are unavailable? Assumption: yes, because backend and asset fetching are out of scope. Blocking: No.
