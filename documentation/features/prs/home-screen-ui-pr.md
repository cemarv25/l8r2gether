# PR Plan: feat: build home screen UI

Status: Draft

## PR

- Title: feat: build home screen UI
- URL: https://github.com/cemarv25/l8r2gether/pull/1
- Draft: yes
- Merge policy: Do not merge by automation

## Related Artifacts

### Features

- `documentation/features/features/feature-001-home-screen-ui.md`

### Tasks

- `documentation/features/tasks/task-001-home-shell-chrome.md`
- `documentation/features/tasks/task-002-home-empty-state.md`
- `documentation/features/tasks/task-003-home-session-list.md`
- `documentation/features/tasks/task-004-home-ui-verification-pr.md`

### Reviews

- `documentation/features/reviews/work/task-001-home-shell-chrome-review.md`
- `documentation/features/reviews/work/task-002-home-empty-state-review.md`
- `documentation/features/reviews/work/task-003-home-session-list-review.md`
- `documentation/features/reviews/work/task-004-home-ui-verification-pr-review.md`

## Summary

Refreshes the Android Compose home screen to closely match the supplied no-sessions and with-sessions mockups. The work updates the shared shell chrome, empty state, populated session list, visual tokens, UI-only avatar placeholder, deterministic thumbnail placeholders, and traceable workflow documentation.

## Testing

- `./gradlew :app:compileDebugKotlin --console=plain`: passed.
- `/Users/cesar/Library/Android/sdk/platform-tools/adb devices`: no devices attached.
- `/Users/cesar/Library/Android/sdk/emulator/emulator -list-avds`: no configured AVDs.

## UI Evidence

- Static implementation review against `screen-mockup-images/home-screen-no-sessions.png`.
- Static implementation review against `screen-mockup-images/home-screen-with-sessions.png`.
- Emulator screenshots were not captured because no Android virtual devices or connected devices are available.

## Risks

- Exact avatar and poster parity is limited because the source assets were not supplied. Mitigation: use Compose-only placeholders that preserve the visual structure without backend or asset fetching.
- Android visual evidence is limited by unavailable emulator/device. Mitigation: compile passed and the QA blocker is documented.

## Follow-Ups

- Capture emulator screenshots once an AVD or device is available.
- Replace Compose placeholder avatar/posters if exact design assets are later provided.
