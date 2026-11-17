# Hayati — حياتي

تطبيق أندرويد عربي (RTL) لتنظيم اليوم والعبادة. **المرحلة الحالية: المهام اليومية (Phase 3)** — المهام تُحفظ في قاعدة بيانات على الجهاز وتظهر في الرئيسية. بقية الميزات (مواقيت الصلاة، القرآن، الأذكار…) لم تُنفَّذ بعد.

Hayati is an Arabic-first, RTL Android app for organizing daily life and worship.
**Current phase: Phase 3 (persistent daily tasks).** Tasks are stored in a Room database and the
Home counter is real. Other features (prayer times, Quran, adhkar, …) are not implemented yet.

- Package: `com.hayati.app` · Version: `0.3.0` · minSdk 26 · target/compileSdk 36
- No account, no internet permission, no analytics/ads/Firebase.

## What works today

- Premium dark Arabic RTL theme (approved palette), Material 3.
- Bottom navigation: الرئيسية، العبادة، المهام، الإحصائيات.
- **Daily tasks (persistent):** add, edit, complete and delete tasks; they are kept after closing the app. Home shows the real tasks progress. (Verified by unit/database tests in CI; not yet tested on a device.)
- Home dashboard, Worship hub, Statistics empty state, Settings (read-only), and
  "coming soon" placeholders for Prayer / Quran / Adhkar / Tasbeeh.
- Values that have no real data source are shown as **"غير متاح بعد"** — nothing is invented.

## Get the APK

The debug APK is built by GitHub Actions (workflow **Android Build**).

1. Open the repository → **Actions** → **Android Build** → pick the latest successful run.
2. Download the artifact **`hayati-debug-apk`** (requires being signed in to GitHub). It is a zip containing `hayati-debug.apk`.
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
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md) · [`docs/ROADMAP.md`](docs/ROADMAP.md) · [`docs/CHANGELOG.md`](docs/CHANGELOG.md)
- [`docs/DECISIONS.md`](docs/DECISIONS.md) · [`docs/TEST_REPORT.md`](docs/TEST_REPORT.md) · [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md)
