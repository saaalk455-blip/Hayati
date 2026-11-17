# Project Status

Last updated: 2026-11-17 (task PHASE3-001)

## Summary

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
| Home prayer / Quran wird / adhkar items | Placeholder ("غير متاح بعد") | No | No | No | – | – |
| Worship hub + section placeholders | Yes | No | No | No | Yes (owner, Phase 2 build) | No |
| Statistics | Empty state only | No | No | No | Yes (owner, Phase 2 build) | No |
| Settings | Read-only info + version | No | No | No | Yes (owner, Phase 2 build) | No |
| Prayer times, Quran, Adhkar, Tasbeeh | Not started | – | – | – | – | – |

"Fully verified" is No everywhere because no automated UI tests exist and no complete on-device
checklist has been recorded.

## Completed tasks

| Task ID | Name | Status | Branch / PR |
|---|---|---|---|
| SETUP-001 | Repository inspection, protocol and documentation | Completed (merged) | PR #1 |
| PHASE2-001 | Android foundation and first debug APK | Completed; owner-tested; PR not yet merged | `feature/android-foundation`, PR #2 |
| PHASE3-001 | Persistent daily tasks (Room) | Completed in CI; awaiting owner device test and review | `feature/tasks-persistence` (stacked on PR #2) |

## Waiting for

1. Owner merges PR #2, then reviews the Phase 3 PR.
2. Owner installs the Phase 3 APK and reports task behavior (add, edit, complete, delete, restart persistence, Home counter).
3. Approved specification for the next phase.
