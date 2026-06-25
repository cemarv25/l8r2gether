# Android Tablet QA: Home Screen

Date: 2026-06-21
Device: `emulator-5554`
AVD: `Medium_Tablet`
Android: 15
Viewport: 2560x1600
Build variant: debug

## Commands

- `/Users/cesar/Library/Android/sdk/emulator/emulator -avd Medium_Tablet -no-snapshot-load -no-audio -no-boot-anim`
- `./gradlew :app:installDebug --console=plain`
- `/Users/cesar/Library/Android/sdk/platform-tools/adb -s emulator-5554 shell am start -n com.l8r2gether.app/.MainActivity`
- `/Users/cesar/Library/Android/sdk/platform-tools/adb -s emulator-5554 exec-out screencap -p > documentation/features/implementation/qa/home-screen-tablet-populated.png`
- `/Users/cesar/Library/Android/sdk/platform-tools/adb -s emulator-5554 logcat -d`

## Evidence

- Screenshot: captured locally at `documentation/features/implementation/qa/home-screen-tablet-populated.png` and intentionally not committed; summarized in PR comment.
- Logcat excerpt: `documentation/features/implementation/qa/home-screen-tablet-populated-logcat-excerpt.txt`

## Result

- Pass: app installed and launched on the tablet emulator.
- Pass: populated home screen rendered at 2560x1600 with left rail, top bar, select-media pill, title/subtitle, two session cards, and bottom-right favorites action.
- Pass: app-specific logcat excerpt shows launch/display events for `com.l8r2gether.app/.MainActivity`; no app process fatal exception was found.
- Limitation: `uiautomator dump` was killed/hung on this emulator, so UI tree XML was not captured.
- Limitation: empty-state capture was attempted by clearing app data, but `pm clear com.l8r2gether.app` hung on the emulator; populated-state evidence was captured successfully.

## Notes

- The emulator log contained unrelated Google Play services and Android hotword enrollment crashes. These did not involve `com.l8r2gether.app`.
