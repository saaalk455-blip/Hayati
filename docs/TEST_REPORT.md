# Test Report

## 2026-11-18 — PHASE5-001 (CI run 44377864036, commit `e6ffa83`)

Run: https://github.com/saaalk455-blip/Hayati/actions/runs/44377864036 — conclusion **success**.
A first run on the previous commit failed: 11 `AdhkarViewModelTest` tests stayed in LOADING because
the test's collector had not subscribed yet (a test-setup bug, fixed with `runCurrent()`).

| Check | Result |
|---|---|
| Tests (`testDebugUnitTest`) | Executed: **170 tests, 0 failures, 0 errors, 0 skipped** (33 new) |
| Debug APK | Built, `hayati-debug.apk` 12,446,114 bytes (run 44377864036, versionName 0.4.0) |
| Final build (run 44378839678, commit `b2d13bb`, conclusion success) | `hayati-debug.apk` 12,446,110 bytes, `aapt2`: versionCode 5, versionName 0.5.0; artifact `hayati-debug-apk` 11,879,717 bytes |

| New class | Tests | Covers |
|---|---|---|
| `AdhkarCounterTest` | 6 | clamping, undo, completion rules |
| `AdhkarJsonParserTest` | 4 | valid entry, default repeat, empty file, rejection of invalid files |
| `AdhkarProgressDaoTest` | 5 | real Room: insert/update, unique key, zero rule, day/category isolation |
| `DatabaseMigrationTest` | 1 (extended) | real v1 file migrated 1→2→3 |
| `AdhkarViewModelTest` | 11 | states, counting, auto-advance, navigation, categories, day change, storage failure |
| `AdhkarContentUiTest` | 7 | Compose: text/counter, callbacks, disabled buttons, finished state, empty/error |

Not executed: device install/run, real adhkar content (none exists), Home integration (not built),
lint. All texts in tests are obvious placeholders. No test results have been fabricated.

## 2026-11-18 — PHASE4-001 (CI run 44222022199, commit `6bab7b6`)

Run: https://github.com/saaalk455-blip/Hayati/actions/runs/44222022199 — conclusion **success**.
Environment: GitHub-hosted runner, Temurin JDK 17, Gradle 8.14.3, AGP 8.9.1, Kotlin 2.1.21.

| Check | Result |
|---|---|
| Unit, Robolectric and Compose UI tests (`testDebugUnitTest`) | Executed: **137 tests, 0 failures, 0 errors, 0 skipped** |
| Debug APK (`assembleDebug`) | Executed, succeeded; `hayati-debug.apk`, 12,386,934 bytes (artifact zip 11,825,968 bytes) |
| APK identity (aapt2) | `com.hayati.app`, versionCode 4, versionName 0.4.0 |
| APK permissions (aapt2) | ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED, plus merged WAKE_LOCK, ACCESS_NETWORK_STATE, FOREGROUND_SERVICE and the internal DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no INTERNET |

| Class | Tests | Covers |
|---|---|---|
| `AdhanPrayerTimeCalculatorTest` | 10 | physical invariants, Hanafi Asr, method difference, adjustment once, DST, invalid input |
| `PrayerSnapshotProviderTest` | 11 | states, next prayer, midnight/day change, tracking merge |
| `PrayerPureLogicTest` | 5 | next-prayer resolver, daily summary, history builder |
| `PrayerLogDaoTest` | 7 | real Room: unique per date+prayer, toggle, revert, persistence |
| `DatabaseMigrationTest` | 1 | v1 -> v2 keeps tasks, adds `prayer_logs` |
| `DataStorePrayerSettingsRepositoryTest` | 3 | settings persist and round-trip |
| `PrayerNotificationCoordinatorTest` | 13 | planning, window, duplicates, late handling, permission, exact vs inexact |
| `AndroidNotificationIntegrationTest` | 8 | Robolectric AlarmManager/NotificationManager shadows, channels |
| `NotificationContentTest` | 4 | Arabic content variants |
| `PrayerViewModelsTest` | 13 | Prayer, History and Settings ViewModels, manual location |
| `PrayerContentUiTest` | 9 | Compose UI: states, tap target, unrecorded label |
| `HomeViewModelTest` | 8 | Home with shared prayer source |
| `TimeFormatterTest` | 4 | clock and countdown formatting |
| Phase 2/3 classes | 41 | `TaskDaoTest` 7, `TaskRepositoryImplTest` 7, `TasksViewModelTest` 14, `TaskTitleTest` 4, `WorshipSectionTest` 3, `HayatiRoutesTest` 4, `HayatiIconsTest` 2 |

Findings fixed during the run: the completion label under the checkbox was not a tap target
(UI test failed; whole control made `toggleable`).

### Not executed

| Check | Reason |
|---|---|
| Install/launch on a device | No device available to Claude |
| Real alarm and notification delivery, sound, reboot rescheduling on hardware | Only Robolectric shadows ran (KI-016) |
| Real location fix and permission dialogs | Needs a device (KI-014) |
| Comparison with an official prayer timetable | No trusted timetable source was available in the sandbox (KI-015) |
| Migration test through Room's helper | Schema export is off (KI-010); a manual migration test ran instead |
| Android lint | Not part of the workflow |
| Opening the downloaded APK artifact | The sandbox cannot download artifact blobs; existence and size confirmed via the GitHub API |

No test results have been fabricated.

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
