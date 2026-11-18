# Changelog

## 0.5.0 — Phase 5: adhkar counter (unreleased, on `feature/adhkar`, stacked on `feature/prayer-system`)

### Added
- Adhkar screen (Worship → الأذكار): categories morning / evening / after prayer, one dhikr at a
  time, tap counter (stops at the required repeat), undo, next/previous, per-category progress, a
  per-day reset, accessibility description with the remaining count.
- Room table `adhkar_progress` (database version 3, `MIGRATION_2_3`), strict JSON content loader.
- 33 new tests (170 total): counter rules, parser, real Room DAO, v1→v2→v3 migration, ViewModel, Compose UI.

### Important limitation
- **No adhkar text is shipped.** `assets/adhkar.json` is empty on purpose; the screen says the
  content is not available yet. Text must come from a reviewed, licensed source (see KI-025).
  Test-only placeholder text exists only in test code.
- Home "أذكار اليوم" is still "غير متاح بعد" (Home integration deferred, KI-026).

### Changed
- App version 0.4.0 → 0.5.0 (versionCode 5). Database 2 → 3.

## 0.4.0 — Phase 4: professional prayer system (unreleased, on `feature/prayer-system`, stacked on `feature/tasks-persistence`)

### Added
- Offline prayer-time engine (adhan2 0.0.5, MIT): 10 methods, Standard/Hanafi Asr, per-prayer
  adjustments applied once, time-zone/DST-aware prayer day.
- Location: automatic (coarse, foreground, one tap, no upload) and manual (country, city, lat, lon,
  time zone) with an offline city list; manual location is never silently replaced.
- Prayer screen: location header, optional Hijri date, next-prayer card with countdown, six-event
  schedule, per-prayer tracking (unrecorded, never "missed"), history screen, daily summary.
- Room `prayer_logs` (DB version 2, `MIGRATION_1_2`); DataStore prayer settings.
- Notifications: per-prayer settings, optional reminder, Arabic content, default/silent channels,
  AlarmManager scheduling (exact when permitted, inexact fallback), reschedule on boot, time-zone
  and time change, app update and permission change, 12-hour WorkManager maintenance.
- Home next-prayer card from the same source as the Prayer screen; tap opens Prayer.
- Prayer settings screen; Settings entry.
- 90 new tests (137 total) incl. Robolectric and Compose UI tests; CI prints failing test messages.
- Docs: `PRAYER_SYSTEM.md`, `DATABASE_SCHEMA.md`.

### Changed
- Permissions added: ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM,
  RECEIVE_BOOT_COMPLETED (WorkManager also merges WAKE_LOCK, ACCESS_NETWORK_STATE,
  FOREGROUND_SERVICE). Still no INTERNET.
- Home `NextPrayer`/`TimeRemaining` placeholder items replaced by the prayer card.
- App version 0.3.0 → 0.4.0 (versionCode 4); added DataStore 1.1.7, WorkManager 2.10.1.

## 0.3.0 — Phase 3: persistent daily tasks (unreleased, on `feature/tasks-persistence`)

### Added
- Room database (`hayati.db`, version 1) with a `tasks` table; `TaskDao`, `TaskRepository` and
  `TaskRepositoryImpl` (data and domain layers).
- Tasks screen: add a task, mark done / reopen, edit (dialog), delete (with confirmation),
  per-day list with progress bar, validation (blank rejected, 120 characters max), error banner
  when storage fails.
- Home "المهام اليومية" now shows real progress ("x من y مكتملة", or "لا توجد مهام اليوم").
- Manual dependency container (`AppContainer`, `HayatiApplication`, `AppViewModelProvider`) and an
  injectable `TimeProvider`.
- 36 new tests (47 total): title validation, repository, ViewModels, and a real Room database
  through Robolectric including a close-and-reopen persistence test.

### Changed
- Home overview model: `OverviewValue.Available(text)` replaced by `OverviewValue.TasksProgress`.
- Kotlin 2.1.20 → 2.1.21 (needed to match KSP `2.1.21-2.0.2`); added Room 2.7.1, KSP,
  kotlinx-coroutines-test, Robolectric 4.14.1, androidx.test core 1.6.1.
- App version 0.2.0 → 0.3.0 (versionCode 3).

## 0.2.0 — Phase 2: Android foundation (on `feature/android-foundation`, PR #2)

### Added
- Android project: Gradle Kotlin DSL, version catalog, Gradle 8.14.3 wrapper, single `:app` module.
- Design system: approved dark palette, Arabic-safe typography (letterSpacing 0), shapes, spacing,
  forced RTL, reusable components and original path-based icons.
- Navigation Compose with bottom bar (الرئيسية، العبادة، المهام، الإحصائيات), no duplicated tabs.
- Screens: Home dashboard (honest "غير متاح بعد" states), Worship hub, Tasks and Statistics empty
  states, read-only Settings with app version, placeholders for Prayer/Quran/Adhkar/Tasbeeh.
- Adaptive launcher icon (original gold crescent), Arabic app label `حياتي` (English label `Hayati`).
- Unit tests (11): HomeViewModel, WorshipSection, routes, icon path parsing.
- GitHub Actions workflow `android-build.yml`: unit tests, debug APK, APK inspection, artifacts.
- `docs/ARCHITECTURE.md`; README rewritten.

### Changed
- `CLAUDE.md` current-phase section updated.

## 0.1.0 — Setup

### Added
- `CLAUDE.md` protocol, `docs/` documentation set and `.gitignore` (SETUP-001, PR #1).
