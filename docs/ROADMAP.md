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

## Phase 4 — Professional prayer system (implemented, awaiting device verification)

- [x] Offline calculation engine, methods, Asr, adjustments, time zone/DST
- [x] Automatic/manual location
- [x] Prayer screen, tracking (Room), history, Home integration
- [x] Notifications (AlarmManager + receivers + WorkManager maintenance)
- [x] 137 tests in CI, debug APK
- [ ] Owner device test: times vs a trusted local timetable, notifications on the real phone
- [ ] Owner merges PR #2, #3, then retargets and reviews the Phase 4 PR
- [ ] Later: Adhan audio, high-latitude rules, geocoded city name, schema export

## Phase 5 — Adhkar counter (structure implemented, content missing)

- [x] Counter, per-day progress, Room v3, screen, tests (170 in CI)
- [ ] Reviewed, licensed adhkar text in `assets/adhkar.json` (blocks real use)
- [ ] Home "أذكار اليوم" card, reminders, device test

## Next phase (recommendation, not started)

Pick one feature per phase and keep persistence with it. Suggested order:

1. ~~Prayer times~~ (done in Phase 4, pending device verification).
2. Quran reading and daily wird (fills the Home wird item).
3. Adhkar and tasbeeh persistence.
4. Statistics from real stored data (tasks already store completion times).
5. Task improvements if wanted: move unfinished tasks to the next day, date navigation, reminders.
6. Release signing, minification and a release APK process; Compose UI tests and lint in CI.

## Later

Defined by future specifications. Explicitly out of scope until approved: cloud sync, accounts,
journal, mood tracking, Pomodoro, habit tracking, audio streaming.
