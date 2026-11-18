# Project Status

Last updated: 2026-11-18 (task PHASE4-001)

## Summary

**Phase 4 (PHASE4-001, version 0.4.0)** adds the prayer system on `feature/prayer-system`, stacked on
the unmerged Phase 3 branch with the owner's explicit approval. CI run 44222022199 (commit
`6bab7b6`) passed: 137 tests, 0 failures; the debug APK `hayati-debug.apk` is 12,386,934 bytes
(versionCode 4, versionName 0.4.0). It has **not** been run on a device, prayer times have **not**
been compared with an official timetable, and real notification delivery is **unverified**. The
definition-of-done item "Phase 3 verified as merged" was **not met**; the owner waived it.
Older Phase 2/3 status follows.

Phase 2 (foundation) was installed and tested on a real phone by the owner, who reported that it
works correctly (owner-reported; no detailed checklist was provided).

Phase 3 (PHASE3-001) adds the first real feature: **persistent daily tasks** stored in a Room
database, with a real task counter on Home. It compiles, passes 47 unit/database tests in CI and
builds a debug APK. It has **not** been run on a device by Claude, and the owner has not yet tested
this build.

## Verified (CI run 44003589733, commit `33ea17a`, conclusion: success)

- Kotlin + KSP (Room) compilation, resource compilation: succeeded.
- Tests: 47 executed, 0 failures, 0 errors, 0 skipped (details in `docs/TEST_REPORT.md`).
  Includes 7 tests against a real Room/SQLite database (Robolectric), one of which closes and
  reopens the database to prove data survives.
- Debug APK `hayati-debug.apk`, 10,903,857 bytes; `aapt2` shows package `com.hayati.app`,
  versionCode 3, versionName 0.3.0, compileSdk/targetSdk 36, label `حياتي`.
- Artifact `hayati-debug-apk` uploaded (retention 30 days).

## Feature status

| Feature | UI | Logic | Persistence | Notifications | Device tested | Fully verified |
|---|---|---|---|---|---|---|
| App shell, theme, RTL, navigation | Yes | Yes | n/a | n/a | Yes (owner, Phase 2 build) | No |
| Tasks: add / edit / complete / delete | Yes | Yes (unit-tested) | Yes (Room, DB-tested) | n/a | No (new build) | No |
| Home "المهام اليومية" counter | Yes | Yes (unit-tested) | Reads from Room | n/a | No (new build) | No |
| Prayer times, next prayer, location, settings | Yes | Yes (unit-tested) | Yes (DataStore, tested) | n/a | No | No |
| Prayer tracking and history | Yes | Yes (unit/Compose-tested) | Yes (Room, DB-tested) | n/a | No | No |
| Prayer notifications | Yes (settings) | Yes (coordinator tested) | Yes (settings) | Scheduling tested with Robolectric shadows; **real delivery unverified** | No | No |
| Home prayer card | Yes | Yes | Shared source | n/a | No | No |
| Home Quran wird / adhkar items | Placeholder ("غير متاح بعد") | No | No | No | – | – |
| Worship hub + section placeholders | Yes | No | No | No | Yes (owner, Phase 2 build) | No |
| Statistics | Empty state only | No | No | No | Yes (owner, Phase 2 build) | No |
| Settings | Read-only info + version | No | No | No | Yes (owner, Phase 2 build) | No |
| Quran, Adhkar, Tasbeeh | Not started | – | – | – | – | – |

"Fully verified" is No everywhere because no automated UI tests exist and no complete on-device
checklist has been recorded.

## Completed tasks

| Task ID | Name | Status | Branch / PR |
|---|---|---|---|
| SETUP-001 | Repository inspection, protocol and documentation | Completed (merged) | PR #1 |
| PHASE2-001 | Android foundation and first debug APK | Completed; owner-tested; PR not yet merged | `feature/android-foundation`, PR #2 |
| PHASE3-001 | Persistent daily tasks (Room) | Completed in CI; awaiting owner device test and review | `feature/tasks-persistence` (PR #3, stacked on PR #2) |
| PHASE4-001 | Professional prayer system | Implemented, CI-verified; not device-tested | `feature/prayer-system` (stacked on PR #3) |

## Waiting for

1. Owner merges PR #2, then PR #3, then the Phase 4 PR is retargeted to `main` and reviewed.
2. Owner installs the Phase 4 APK and reports: prayer times vs a trusted local timetable, location, tracking, notifications (including after reboot and with battery saver), and the Phase 3 task behavior.
3. Approved specification for the next phase.
