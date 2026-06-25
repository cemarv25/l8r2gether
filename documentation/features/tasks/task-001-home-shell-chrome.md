# Task: task-001-home-shell-chrome Home Shell Chrome

Status: Draft
Parent feature: `documentation/features/features/feature-001-home-screen-ui.md`

## Source References

| Source | Reference | Requirement / Design Detail |
| --- | --- | --- |
| Feature | `documentation/features/features/feature-001-home-screen-ui.md` | Match shell, rail, top bar, color tokens, typography sizing, and shared UI primitives. |
| Design | `screen-mockup-images/home-screen-no-sessions.png` | Left rail with `Us`, selected Cinema tile, Moments, Lounge, bottom favorites/help; top brand/navigation/actions. |
| Design | `screen-mockup-images/home-screen-with-sessions.png` | Compact top bar with brand at left and actions at right; no full top nav visible in populated screenshot. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/AppShell.kt` | Shell wrapper for rail/top/content. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtNavRail.kt` | Left rail implementation. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/shell/LtTopBar.kt` | Top bar implementation. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt` | Color tokens. |
| Code | `app/src/main/java/com/l8r2gether/app/ui/theme/LtTypography.kt` | Typography tokens. |

## Objective

Align the shared home shell chrome and reusable visual tokens with the screenshots without changing navigation behavior.

## Requirements

- Tune or add UI color tokens for the warm canvas, pale rail, selected rail tile, primary mauve, green secondary text, muted icon color, card border, and soft shadows.
- Adjust `AppShell` only as needed for screenshot-like rail/top/content sizing and snackbar offset.
- Update `LtNavRail` to match rail width, selected tile shape/color, icon/label spacing, bottom favorites circle, and Help placement.
- Update `LtTopBar` to match brand typography/color, top navigation spacing, right-side icons, divider, and UI-only avatar placeholder.
- Preserve current callbacks for Cinema, Moments, Lounge, Favorites, Settings, and Help.
- Keep changes UI-only.

## Non-Goals

- No auth/profile functionality for the avatar.
- No settings/favorites/help feature implementation.
- No backend or repository changes.
- No broad unrelated redesign outside shell-wrapped UI.

## Likely Files / Modules

- `app/src/main/java/com/l8r2gether/app/ui/theme/LtColor.kt`: visual tokens.
- `app/src/main/java/com/l8r2gether/app/ui/theme/LtTypography.kt`: heading/brand typography tuning if needed.
- `app/src/main/java/com/l8r2gether/app/ui/shell/AppShell.kt`: layout shell spacing.
- `app/src/main/java/com/l8r2gether/app/ui/shell/LtNavRail.kt`: rail visual implementation.
- `app/src/main/java/com/l8r2gether/app/ui/shell/LtTopBar.kt`: top bar visual implementation.
- `app/src/main/res/values/strings.xml`: only if a content description or visible label needs adjustment.

## Dependencies And Ordering

- Depends on approved feature review.
- Must be implemented before task-002 and task-003 so home states can reuse aligned tokens and shell sizing.

## Test Scenarios

- Build/compile: run the relevant Gradle compile or assemble task and confirm shell changes compile.
- Empty state visual smoke: launch app with no sessions and confirm left rail/top bar remain visible and aligned.
- Populated state visual smoke: launch app with sessions or preview state and confirm top/rail chrome does not overlap content.

## Android QA Plan

- Applies: yes
- Skill: `android-emulator-qa` when available
- Build variant: debug
- Emulator/device: available emulator at verification time
- Flow to validate: app launch to home; inspect rail/top bar in empty or populated state
- Evidence required: screenshot and UI-tree summary during final verification task, or documented reason if emulator unavailable

## Acceptance Criteria

- [ ] Left navigation rail visually approximates both screenshots and keeps existing click behavior.
- [ ] Top bar visually approximates the screenshots and keeps existing click behavior.
- [ ] Shared color/typography tokens support the mockup palette without breaking compile.
- [ ] No backend, auth, or data behavior is added.
- [ ] Shell/content layout does not overlap at the target screenshot dimensions.

## Review Checklist

- [ ] Aligns with parent feature
- [ ] Meets requirements and non-goals
- [ ] Handles relevant states and errors
- [ ] Tests or verification cover acceptance criteria
- [ ] Android emulator QA evidence is captured when applicable
- [ ] Avoids unrelated refactors

## Implementation Notes

Prefer narrow theme additions and local helpers over changing global Material defaults in ways that unintentionally affect other screens.

## Open Questions

- [ ] None blocking.
