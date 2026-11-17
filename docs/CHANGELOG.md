# Changelog

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
