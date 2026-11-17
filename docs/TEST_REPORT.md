# Test Report

## 2026-11-17 — PHASE3-001 (CI run 44003589733, commit `33ea17a`)

Run: https://github.com/saaalk455-blip/Hayati/actions/runs/44003589733 — conclusion **success**.
Environment: GitHub-hosted `ubuntu-latest`, Temurin JDK 17.0.21, Gradle 8.14.3, AGP 8.9.1,
Kotlin 2.1.21, KSP 2.1.21-2.0.2, Room 2.7.1.

| Check | Result |
|---|---|
| KSP (Room annotation processing) | Executed, succeeded |
| Kotlin and Java compilation | Executed, succeeded; no `w:` warning lines in the log |
| Unit and database tests (`testDebugUnitTest`) | Executed: **47 tests, 0 failures, 0 errors, 0 skipped** |
| Debug APK (`assembleDebug`) | Executed, succeeded |
| APK file | `hayati-debug.apk`, 10,903,857 bytes |
| APK identity (aapt2) | `com.hayati.app`, versionCode 3, versionName 0.3.0, compileSdk/targetSdk 36, label `حياتي` |
| APK permissions (aapt2) | Only the internal `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` merged by AndroidX; no INTERNET |

Tests by class:

| Class | Tests | What it covers |
|---|---|---|
| `TaskDaoTest` | 7 | Real Room/SQLite via Robolectric (SDK 34): insert, per-day filter, ordering, update, complete/reopen, delete, **data survives closing and reopening the database** |
| `TaskRepositoryImplTest` | 7 | Trimming, blank rejection, timestamps, per-day query, ordering (fake DAO) |
| `TasksViewModelTest` | 14 | Add/validate/clear, edit flow, cancel, delete confirmation, toggle, day change, storage failure surfaced |
| `HomeViewModelTest` | 6 | Real tasks counter, other items stay unavailable, day change |
| `TaskTitleTest` | 4 | Trim, blank, max length |
| `HayatiIconsTest` | 2 | Icon paths parse |
| `WorshipSectionTest` | 3 | Section routes |
| `HayatiRoutesTest` | 4 | Navigation routes |

### Not executed

| Check | Reason |
|---|---|
| Install and launch of this build on a device | No device available to Claude; owner has not yet tested this build |
| Tasks screen on screen (keyboard, dialogs, RTL text entry, scrolling) | Needs a device; no Compose UI tests exist |
| Real-device database persistence across app restarts | Covered on the JVM only (reopen test); not on a phone |
| Database migrations | None exist yet (schema version 1; schema export is off) |
| Android lint | Not part of the workflow |
| Opening the downloaded APK artifact | The authoring sandbox cannot download GitHub artifact blobs; existence and size were confirmed through the GitHub API |

No test results have been fabricated.

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
