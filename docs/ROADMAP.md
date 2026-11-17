# Roadmap

Only approved specifications define scope. Items below are status, not commitments.

## Phase 0 — Setup (done)

- [x] Repository inspection, protocol and documentation (PR #1 merged)

## Phase 2 — Android foundation and first APK (done, owner-tested)

- [x] Gradle Kotlin DSL project, version catalog, wrapper
- [x] Compose + Material 3 dark theme with approved palette, forced RTL
- [x] Bottom navigation and placeholder screens
- [x] GitHub Actions: unit tests + debug APK artifact
- [x] Owner device test: reported working
- [ ] Owner merges PR #2

## Phase 3 — Persistent daily tasks (implemented, awaiting device verification)

- [x] Room database, repository, manual dependency container
- [x] Tasks screen: add, edit, complete, delete (with confirmation), per-day list, progress
- [x] Home task counter reads real data
- [x] Unit tests, repository tests, Room database tests, CI APK
- [ ] Owner device test of the Phase 3 APK
- [ ] Owner review and merge

## Next phase (recommendation, not started)

Pick one feature per phase and keep persistence with it. Suggested order:

1. Prayer times (calculation engine, location permission flow, notification channel/scheduling) —
   fills the "next prayer" and "time remaining" Home items.
2. Quran reading and daily wird (fills the Home wird item).
3. Adhkar and tasbeeh persistence.
4. Statistics from real stored data (tasks already store completion times).
5. Task improvements if wanted: move unfinished tasks to the next day, date navigation, reminders.
6. Release signing, minification and a release APK process; Compose UI tests and lint in CI.

## Later

Defined by future specifications. Explicitly out of scope until approved: cloud sync, accounts,
journal, mood tracking, Pomodoro, habit tracking, audio streaming.
