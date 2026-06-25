# Task: task-003-home-session-list Home Session List

Status: Draft
Parent feature: `documentation/features/features/feature-001-home-screen-ui.md`

## Source References

| Source | Reference | Requirement / Design Detail |
| --- | --- | --- |
| Feature | `documentation/features/features/feature-001-home-screen-ui.md` | Match the populated session-list state and cards. |
| Design | `screen-mockup-images/home-screen-with-sessions.png` | Select-new-media pill, centered title/subtitle, two rounded session cards, thumbnails, resume/timestamp buttons, notes icon, bottom-right favorites FAB. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeSessionList.kt` | Populated home UI implementation target. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeSampleSessions.kt` | Existing sample sessions match mockup titles/timestamps. |
| Code | `app/src/main/java/com/l8r2gether/app/viewmodel/HomeViewModel.kt` | Existing session UI state must remain unchanged. |

## Objective

Rework the populated home content and session card UI to closely match the supplied returning-user mockup while preserving current local interactions.

## Requirements

- Center the select-new-media pill above the title with matching shape, fill, icon, text, and arrow treatment.
- Match the title/subtitle typography hierarchy and spacing.
- Render session cards as wide white rounded surfaces with subtle shadow, left thumbnail, middle text/actions, and right notes icon.
- Use UI-only thumbnail approximations that feel close to the screenshots without fetching poster assets.
- Keep most-recent card using `Resume Session`; older cards use `Resume`.
- Preserve `onSelectNewMedia`, `onResumeSession`, `onSyncNow`, and `onFavoritesClick` behavior.
- Place the floating favorites button at bottom-right with screenshot-like size, color, and shadow.
- Keep changes UI-only.

## Non-Goals

- No real poster retrieval, media search, or remote metadata.
- No changes to session repository/storage behavior.
- No implementation of notes/favorites beyond current callbacks/placeholders.

## Likely Files / Modules

- `app/src/main/java/com/l8r2gether/app/ui/home/HomeSessionList.kt`: primary UI implementation.
- `app/src/main/java/com/l8r2gether/app/ui/home/HomeSampleSessions.kt`: only if preview/sample presentation needs UI-only tuning.
- `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt`: only if task-001 leaves a missing local token.
- `app/src/main/res/values/strings.xml`: only if text/content descriptions need small adjustments.

## Dependencies And Ordering

- Depends on task-001 shell/tokens.
- Can be implemented after or alongside task-002 only if file ownership is sequenced; primary file is disjoint from empty state.
- Should complete before task-004 verification.

## Test Scenarios

- Populated state composition: with two sessions, app shows select-new-media pill, title/subtitle, two session cards, and favorites FAB.
- Action preservation: select-new-media, resume, sync-now, and favorites callbacks are still invoked from their controls.
- Visual smoke: cards remain centered and non-overlapping at target screenshot dimensions.

## Android QA Plan

- Applies: yes
- Skill: `android-emulator-qa` when available
- Build variant: debug
- Emulator/device: available emulator at verification time
- Flow to validate: launch app with populated or preview/sample session state when practical
- Evidence required: screenshot and UI-tree summary during final verification task, or documented reason if emulator unavailable

## Acceptance Criteria

- [ ] Populated state structure closely matches `home-screen-with-sessions.png`.
- [ ] Session cards match screenshot hierarchy, proportions, actions, and icon placement as closely as practical.
- [ ] Floating favorites button is visible bottom-right and does not obscure cards.
- [ ] Existing populated-state callbacks are preserved.
- [ ] No backend, API, auth, database, or data-loading behavior is added.

## Review Checklist

- [ ] Aligns with parent feature
- [ ] Meets requirements and non-goals
- [ ] Handles relevant states and errors
- [ ] Tests or verification cover acceptance criteria
- [ ] Android emulator QA evidence is captured when applicable
- [ ] Avoids unrelated refactors

## Implementation Notes

Use deterministic UI-only thumbnail styling from `contentKey` so real sessions still get stable visual placeholders.

## Open Questions

- [ ] None blocking.
