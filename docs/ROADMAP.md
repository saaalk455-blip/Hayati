# Roadmap

Only approved specifications define scope. Items below are status, not commitments.

## Phase 0 — Setup (done)

- [x] Repository inspection, protocol and documentation (PR #1 merged)

## Phase 2 — Android foundation and first APK (implemented, awaiting device verification)

- [x] Gradle Kotlin DSL project, version catalog, wrapper
- [x] Compose + Material 3 dark theme with approved palette, forced RTL
- [x] Bottom navigation and placeholder screens (Home, Worship, Tasks, Statistics, Settings)
- [x] GitHub Actions: unit tests + debug APK artifact
- [ ] Owner device test of the APK (launch, RTL, navigation, back button)
- [ ] Owner review and merge of the pull request

## Next phase (recommendation, not started)

Choose one first real feature and add its persistence layer with it. Suggested order:

1. Tasks (Room + repository + ViewModel + tests) — no external data needed, so it validates the
   data/domain layers, DI choice and database tests end to end.
2. Prayer times (calculation engine, location permission flow, notifications).
3. Quran reading and daily wird.
4. Adhkar and tasbeeh persistence.
5. Statistics from real stored data.
6. Release signing, minification and release APK process.

## Later

Defined by future specifications. Explicitly out of scope until approved: cloud sync, accounts,
journal, mood tracking, Pomodoro, habit tracking, audio streaming.
