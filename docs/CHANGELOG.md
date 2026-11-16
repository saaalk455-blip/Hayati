# Changelog

## 0.2.0 — Phase 2: Android foundation (unreleased, on `feature/android-foundation`)

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
