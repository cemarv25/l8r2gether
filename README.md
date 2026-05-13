# LaterTogether (Android companion)

Native Kotlin + Jetpack Compose tablet companion that anchors chat to **media time** while you watch in other apps. This repo is a multi-module Gradle project (`:domain`, `:data`, `:app`).

---

## Requirements

| Requirement            | Notes                                                                                                                                                                               |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **JDK**                | **17 or newer** (recommended: **JDK 17 or 18**). The `:domain` module uses JVM toolchain **18**; `:app` compiles with Java **17**. Install a JDK and point tools at it (see below). |
| **Android SDK**        | Install via **Android Studio** (simplest) or **Android SDK command-line tools**. You need **SDK Platform** matching **`compileSdk 35`** and **Android SDK Build-Tools**.            |
| **Device or emulator** | **API 26+** (`minSdk 26`). A **tablet or large emulator** in landscape matches the intended UX.                                                                                     |

Optional:

| Optional             | Notes                                                                                                                             |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Supabase project** | For live messages/auth. Without keys, the app can use placeholder/in-memory behavior depending on configuration—see **Supabase**. |

---

## 1. Install the JDK

**macOS** (pick one):

- **Android Studio** bundles a suitable JBR; you can use it only from Android Studio, or
- Install **Temurin 17/18** or use **Homebrew**: `brew install openjdk@17`, then add `export JAVA_HOME="$(/usr/libexec/java_home -v 17)"` to your shell profile.

Verify:

```bash
java -version
```

Gradle uses `JAVA_HOME` if set. To build `:domain` with its toolchain, JDK **18** is ideal if you see toolchain resolution errors:

```bash
/usr/libexec/java_home -V    # list installed JDKs
export JAVA_HOME="$(/usr/libexec/java_home -v 18)"   # or 17 if that’s what you use consistently
```

---

## 2. Install the Android SDK

### Option A — Android Studio (recommended)

1. Download and install [Android Studio](https://developer.android.com/studio).
2. Open **Android Studio** → **Settings / Preferences** → **Languages & Frameworks** → **Android SDK**.
3. On the **SDK Platforms** tab, install **Android 15 (API 35)** (or the platform that matches `compileSdk 35` in the SDK manager).
4. On the **SDK Tools** tab, ensure **Android SDK Build-Tools**, **Platform-Tools**, and **Android SDK Command-line Tools** are installed.
5. Note the **Android SDK Location** shown at the top (e.g. `~/Library/Android/sdk` on macOS).

### Option B — Command-line `sdkmanager` only

1. Create a directory for the SDK, e.g. `~/Library/Android/sdk`.
2. Download [command line tools](https://developer.android.com/studio#command-line-tools-only) for macOS, unpack, and use `sdkmanager` to install platform 35 and build-tools (see Android docs for the exact package names).

---

## 3. Point Gradle at the SDK

From the repo root:

```bash
cp local.properties.example local.properties
```

Edit **`local.properties`** and set **`sdk.dir`** to your SDK path (no quotes needed on macOS/Linux):

```properties
sdk.dir=/Users/YOU/Library/Android/sdk
```

You can instead set **`ANDROID_HOME`** (or **`ANDROID_SDK_ROOT`**) to the same path; Gradle also honors those for many setups, but **`sdk.dir` in `local.properties`** is the standard for this project.

**Do not commit `local.properties`** (it is gitignored).

---

## 4. Supabase (optional, for real backend)

In **`local.properties`**, set:

```properties
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=your_anon_key
```

Apply migrations from **`supabase/migrations/`** to your Supabase project (see [`supabase/README.md`](supabase/README.md)). If these are empty, the app may fall back to stub/local behavior—check `LaterTogetherApp` / `BuildConfig` wiring.

---

## 5. Build

From the repository root (first time: ensure **`gradlew`** is executable: `chmod +x gradlew`):

```bash
./gradlew :domain:test
./gradlew :app:assembleDebug
```

- **`:domain:test`** runs JVM unit tests (no Android SDK needed for this module alone if the toolchain resolves).
- **`:app:assembleDebug`** requires a valid **`sdk.dir`** and produces `app/build/outputs/apk/debug/app-debug.apk`.

---

## 6. Run on a device or emulator

### Emulator

1. In Android Studio: **Device Manager** → create a **tablet** or phone AVD with **API 26+**.
2. Start the AVD.

### Physical device

1. Enable **Developer options** and **USB debugging**.
2. Connect via USB (or use wireless debugging).

### Install and launch

```bash
./gradlew :app:installDebug
adb shell am start -n com.latertogether.app/.MainActivity
```

Or open the project in **Android Studio** and use **Run** ▶ on the chosen device.

---

## 7. Testing summary

| What                        | Command / action                                             |
|-----------------------------|--------------------------------------------------------------|
| **Domain unit tests**       | `./gradlew :domain:test`                                     |
| **Compile Android modules** | `./gradlew :data:compileDebugKotlin :app:compileDebugKotlin` |
| **Debug APK**               | `./gradlew :app:assembleDebug`                               |
| **Instrumented UI tests**   | None are checked in under `androidTest/` yet.                |

MediaSession and Accessibility features require **appropriate permissions and user toggles** on the device (notification listener / accessibility); see in-app settings and `AndroidManifest.xml`.

---

## Troubleshooting

| Issue                      | What to try                                                                                       |
|----------------------------|---------------------------------------------------------------------------------------------------|
| **SDK location not found** | Set **`sdk.dir`** in **`local.properties`** to the exact path from Android Studio’s SDK settings. |
| **JDK / toolchain errors** | Set **`JAVA_HOME`** to JDK 17 or 18; run **`./gradlew -version`** to see which JVM Gradle uses.   |
| **License errors**         | Run **`sdkmanager --licenses`** (from Android SDK cmdline-tools) and accept licenses.             |
| **Gradle daemon / cache**  | **`./gradlew --stop`** then rebuild.                                                              |

---

## Project layout

- **`domain/`** — Pure Kotlin: session time estimation, fusion, chat ledger (JVM tests).
- **`data/`** — Supabase client + repository mapping.
- **`app/`** — Compose UI, MediaSession / accessibility glue.
- **`supabase/`** — SQL migrations and backend notes.

For product behavior, see **`latertogether-companion-sync-spec.md`** in this repo.
