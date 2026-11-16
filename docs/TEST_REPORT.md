# Test Report

## 2026-11-16 — PHASE2-001 (CI run 43948834416, commit `09175d0`)

Run: https://github.com/saaalk455-blip/Hayati/actions/runs/43948834416 — conclusion **success**.
Environment: GitHub-hosted `ubuntu-latest`, Temurin JDK 17.0.21, Gradle 8.14.3, AGP 8.9.1.

| Check | Result |
|---|---|
| Gradle configuration | Executed, succeeded |
| Kotlin compilation (`compileDebugKotlin`) | Executed, succeeded; no `w:`/warning lines matched in the log |
| Android resource compilation (`mergeDebugResources`) | Executed, succeeded |
| Compose compilation | Executed as part of Kotlin compilation, succeeded |
| Unit tests (`testDebugUnitTest`) | Executed: 11 tests, 0 failures, 0 errors, 0 skipped |
| Debug APK (`assembleDebug`) | Executed, succeeded |
| APK output path and size | `app/build/outputs/apk/debug/hayati-debug.apk`, 10,581,314 bytes |
| APK package name | `com.hayati.app` (aapt2 badging) |
| APK versions | versionCode 2, versionName 0.2.0, compileSdk 36, targetSdk 36 |
| APK label / launcher activity | `حياتي` / `com.hayati.app.MainActivity` |
| APK permissions (aapt2) | Only `com.hayati.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, an internal permission merged by AndroidX; no INTERNET or dangerous permission |

Unit tests by class:

| Class | Tests |
|---|---|
| `HomeViewModelTest` | 2 |
| `WorshipSectionTest` | 3 |
| `HayatiRoutesTest` | 4 |
| `HayatiIconsTest` | 2 |

### Not executed

| Check | Reason |
|---|---|
| Install and launch on emulator/device | No device or emulator available to Claude |
| Navigation, back button, RTL appearance on a device | Same; needs owner testing |
| Compose UI / instrumented tests | None written in Phase 2 |
| Database tests | No database exists yet |
| Android lint | Not part of the workflow yet |
| minSdk value inside the APK | The workflow's `aapt2` filter did not print the `minSdkVersion` line; minSdk 26 is set in `app/build.gradle.kts` but was not read back from the APK |
| Opening the downloaded APK artifact | The authoring sandbox cannot download GitHub artifact blobs; the artifact's existence, size and metadata were confirmed through the GitHub API instead |

No test results have been fabricated.

## 2026-11-16 — SETUP-001

Docs-only task; no code, no build. Nothing to execute.
