# Hayati — حياتي

تطبيق أندرويد عربي (RTL) لتنظيم اليوم والعبادة. **المرحلة الحالية: نظام الصلاة (Phase 4)** — حساب مواقيت الصلاة دون إنترنت، تتبّع الصلوات، وتنبيهات. المهام اليومية (Phase 3) محفوظة في قاعدة بيانات. القرآن والأذكار والإحصائيات لم تُنفَّذ بعد.

Hayati is an Arabic-first, RTL Android app for organizing daily life and worship.
**Current phase: Phase 4 (prayer system).** Offline prayer-time calculation, location (automatic or
manual), prayer tracking, and notifications. Phase 3 (persistent tasks) is included. Quran, adhkar
and statistics are not implemented yet.

- Package: `com.hayati.app` · Version: `0.4.0` · minSdk 26 · target/compileSdk 36
- No account, no internet permission, no analytics/ads/Firebase. Location is coarse, requested only
  when you tap "detect my location", and never uploaded. See `docs/PRAYER_SYSTEM.md`.

## What works today

- Premium dark Arabic RTL theme (approved palette), Material 3.
- Bottom navigation: الرئيسية، العبادة، المهام، الإحصائيات.
- **Daily tasks (persistent):** add, edit, complete and delete tasks; they are kept after closing the app. Home shows the real tasks progress. (Verified by unit/database tests in CI; not yet tested on a device.)
- **Prayer system (Phase 4):** times for Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha computed on-device
  with adhan2 (MIT); 10 calculation methods; Standard/Hanafi Asr; per-prayer minute adjustments;
  automatic or manual location; next-prayer countdown; per-prayer tracking (never auto-completed;
  a passed, unrecorded prayer is "unrecorded", not "missed"); history; notifications with optional
  reminder. Verified by unit/Robolectric/Compose tests in CI. **Not yet tested on a device; times
  are not yet compared with an official timetable** (see `docs/KNOWN_ISSUES.md`).
- Home dashboard, Worship hub, Statistics empty state, Settings, and
  "coming soon" placeholders for Quran / Adhkar / Tasbeeh.
- Values that have no real data source are shown as **"غير متاح بعد"** — nothing is invented.

## Get the APK

The debug APK is built by GitHub Actions (workflow **Android Build**).

1. Open the repository → **Actions** → **Android Build** → pick the latest successful run.
2. Download the artifact **`hayati-debug-apk`** (requires being signed in to GitHub). It is a zip containing `hayati-debug.apk` (the pipeline's file name; Phase 4 was suggested as `hayati-phase-4-prayer-debug.apk`).
3. Extract it, copy the APK to the phone, open it and allow *Install unknown apps* for the app you opened it with (Files, Chrome, …).
4. The APK is debug-signed, so Google Play Protect may show a warning: choose *Install anyway*.

## Build locally

Requirements: JDK 17, Android SDK with platform 36 and build-tools 35.0.0.

```bash
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # app/build/outputs/apk/debug/hayati-debug.apk
```

## Documentation

- [`CLAUDE.md`](CLAUDE.md) — permanent development protocol
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — structure and dependency versions
- [`docs/PRAYER_SYSTEM.md`](docs/PRAYER_SYSTEM.md) — prayer engine, location, notifications, limits
- [`docs/DATABASE_SCHEMA.md`](docs/DATABASE_SCHEMA.md) — Room schema and migrations
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md) · [`docs/ROADMAP.md`](docs/ROADMAP.md) · [`docs/CHANGELOG.md`](docs/CHANGELOG.md)
- [`docs/DECISIONS.md`](docs/DECISIONS.md) · [`docs/TEST_REPORT.md`](docs/TEST_REPORT.md) · [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md)
