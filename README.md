# l8r2gether (Android companion)

Native Kotlin + Jetpack Compose tablet companion for timestamp-anchored async chat. This greenfield build implements the **Home screen** (empty state, session library, Sync now) per `l8r2gether-companion-sync-spec.md` v0.1.7.

## Requirements

- JDK 17+ (JDK 18 works with the current `:domain` toolchain)
- Android SDK (API 35 recommended)
- `local.properties` with `sdk.dir` (copy from `local.properties.example`)

Set `JAVA_HOME` if Gradle cannot find a JDK, e.g. `export JAVA_HOME=$(/usr/libexec/java_home -v 18)`.

## Build and test

```bash
./gradlew :domain:test
./gradlew :app:assembleDebug
```

Install the debug APK on a tablet emulator (landscape, 1280×800 recommended) and verify Home flows.

## Modules

| Module | Role |
|--------|------|
| `:domain` | Session model, media time parsing, library helpers |
| `:app` | Compose UI, DataStore session persistence, navigation |
