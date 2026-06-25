# Task: task-002-home-empty-state Home Empty State

Status: Draft
Parent feature: `documentation/features/features/feature-001-home-screen-ui.md`

## Source References

| Source | Reference | Requirement / Design Detail |
| --- | --- | --- |
| Feature | `documentation/features/features/feature-001-home-screen-ui.md` | Match the no-sessions home state. |
| Design | `screen-mockup-images/home-screen-no-sessions.png` | Centered icon tile, soft glow, headline/body, primary pill action, library link, lower feature hints. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/home/HomeEmptyState.kt` | Empty-state implementation target. |
| Code | `app/src/main/res/values/strings.xml` | Existing text matches mockup copy. |

## Objective

Rework the no-sessions home content to visually match the supplied empty-state mockup while preserving its existing actions.

## Requirements

- Use the screenshot’s centered vertical composition and proportional spacing.
- Create a large rounded white media tile with clapperboard/play-like icon treatment and two small heart accents.
- Add a soft radial glow behind the hero tile on the warm background.
- Match headline, body copy, primary pill button, and shared-library link hierarchy.
- Position the feature hint icons/labels near the lower content area without overlapping the main CTA.
- Preserve `onStartNewSession` and `onBrowseLibrary` behavior.
- Keep the implementation UI-only.

## Non-Goals

- No new session backend behavior.
- No library browsing implementation beyond the existing callback.
- No image asset fetching.

## Likely Files / Modules

- `app/src/main/java/com/l8r2gether/app/ui/home/HomeEmptyState.kt`: primary UI implementation.
- `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt`: only if task-001 leaves a missing local token.
- `app/src/main/res/values/strings.xml`: only if text/content descriptions need small adjustments.

## Dependencies And Ordering

- Depends on task-001 shell/tokens for palette and typography.
- Should be implemented before task-004 verification.

## Test Scenarios

- Empty state composition: with empty `HomeUiState`, app shows headline, body, primary action, library link, and feature hints.
- Action preservation: primary action invokes `onStartNewSession`; library link invokes `onBrowseLibrary`.
- Responsive visual smoke: content remains centered and non-overlapping at tablet-like screenshot dimensions.

## Android QA Plan

- Applies: yes
- Skill: `android-emulator-qa` when available
- Build variant: debug
- Emulator/device: available emulator at verification time
- Flow to validate: launch app in empty state
- Evidence required: screenshot and UI-tree summary during final verification task, or documented reason if emulator unavailable

## Acceptance Criteria

- [ ] Empty state structure closely matches `home-screen-no-sessions.png`.
- [ ] Text hierarchy and button/link styling approximate the screenshot.
- [ ] Feature hints are visible near the lower content area and do not overlap other elements.
- [ ] Existing empty-state callbacks are preserved.
- [ ] No backend, API, auth, database, or data-loading behavior is added.

## Review Checklist

- [ ] Aligns with parent feature
- [ ] Meets requirements and non-goals
- [ ] Handles relevant states and errors
- [ ] Tests or verification cover acceptance criteria
- [ ] Android emulator QA evidence is captured when applicable
- [ ] Avoids unrelated refactors

## Implementation Notes

Keep any custom drawing simple Compose primitives/icons so the UI remains maintainable without introducing external assets.

## Open Questions

- [ ] None blocking.
