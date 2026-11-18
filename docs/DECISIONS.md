# Architectural & Process Decisions

## D-001 — Adopt the permanent collaboration protocol (2026-11-16)

- Status: Accepted (provided by the owner)
- Decision: Work follows `CLAUDE.md`: feature branches, no pushes of unfinished work to `main`,
  no force pushes, mandatory final report, documentation updated after every task.

## D-002 — Target architecture (2026-11-16)

- Status: Accepted (stated in the protocol)
- Decision: Kotlin, Jetpack Compose, MVVM, Clean Architecture, SOLID, dependency injection,
  lifecycle-aware state, RTL-ready layouts, real persistent data for completed features.

## D-003 — Public repository hygiene (2026-11-16)

- Status: Accepted
- Decision: `.gitignore` excludes `local.properties`, keystores, signing configs, `google-services.json`,
  `.env` files and build outputs. Secrets are never committed. Generated APKs are not committed;
  they are delivered as GitHub Actions artifacts.

## D-004 — SDK levels (2026-11-16, Phase 2)

- Decision: minSdk 26, targetSdk 36, compileSdk 36.
- Why: minSdk 26 (Android 8.0) allows adaptive vector launcher icons with no PNG fallbacks and
  covers the large majority of active devices. SDK 36 is supported by AGP 8.9.1.
- Consequence: Android 7.x and older cannot install the app.

## D-005 — Toolchain versions (2026-11-16, Phase 2)

- Decision: AGP 8.9.1, Kotlin 2.1.20 (Compose compiler Gradle plugin), Gradle 8.14.3 wrapper,
  JDK 17 in CI, Compose BOM 2025.04.01. All versions in `gradle/libs.versions.toml`.
- Why: stable, mutually compatible releases; Kotlin 2.x uses the Compose compiler plugin so no
  separate compiler-version matching is needed. These are not the newest releases; upgrading is a
  separate task.

## D-006 — No Hilt, Room or DataStore yet (2026-11-16, Phase 2)

- Decision: ViewModels are constructed with the default factory; no DI framework, no database.
- Why: nothing in Phase 2 stores data or has dependencies to inject, and an unused annotation
  processor adds build time and failure risk. Both will be added with the first feature that needs
  them (see roadmap). Layering is still enforced through packages.

## D-007 — Build verification through GitHub Actions (2026-11-16, Phase 2)

- Decision: `.github/workflows/android-build.yml` runs unit tests and builds the debug APK on
  pushes to `main`/`feature/**`/`fix/**`/`refactor/**`, on PRs to `main`, and manually.
- Why: the authoring environment has no Android SDK and no access to Maven repositories, so builds
  cannot run locally there.
- Note: `android-actions/setup-android` is given an explicit package list, because its default
  list includes the obsolete `tools` package and fails.

## D-008 — Forced RTL and system Arabic font (2026-11-16, Phase 2)

- Decision: `HayatiTheme` forces `LayoutDirection.Rtl`; typography uses the system font family with
  letterSpacing 0.
- Why: the UI is Arabic-only for now, so it must be RTL even on a device set to another language;
  Android's system font shapes Arabic correctly and avoids bundling a font license.
- Revisit: when a language setting or a custom brand font is introduced.

## D-009 — Honest placeholders (2026-11-16, Phase 2)

- Decision: values without a real data source are modeled as `OverviewValue.Unavailable` and shown
  as "غير متاح بعد". No fake prayer times, Quran progress, statistics or tasks.
- Settings contains no toggles, because nothing is persisted yet.

## D-010 — Manual dependency injection instead of Hilt (2026-11-17, Phase 3)

- Decision: constructor injection through a small `AppContainer` created by `HayatiApplication`,
  with one `AppViewModelProvider` factory. No Hilt.
- Why: the object graph is tiny (database, one repository, a time provider, two ViewModels). Hilt
  would add a second annotation processor and build-time risk for no present benefit, and every
  class already takes its dependencies through its constructor, so migrating later is mechanical.
- Revisit: when the graph grows (several repositories, workers, notifications) or when
  instrumented tests need to replace bindings.

## D-011 — Tasks are scoped to a calendar day (2026-11-17, Phase 3)

- Decision: a task stores `epochDay` (device time zone) and the Tasks screen and Home counter show
  only today's tasks. Day changes are picked up when the screen starts (`refreshDay`).
- Why: the product text calls them "المهام اليومية"; per-day storage also gives the future
  Statistics feature real per-day data (creation and completion times are stored).
- Consequence: unfinished tasks are not carried over automatically (KI-011).

## D-012 — Room with KSP; Kotlin 2.1.21 (2026-11-17, Phase 3)

- Decision: Room 2.7.1 with the KSP plugin `2.1.21-2.0.2`; Kotlin moved from 2.1.20 to 2.1.21
  because KSP versions are tied to a Kotlin version and a `2.1.21` KSP release was the one confirmed
  to exist. Schema export is off for version 1 (KI-010).
- Note: newer Room/Kotlin releases exist; upgrading is a separate task.

## D-013 — Database tests on the JVM with Robolectric (2026-11-17, Phase 3)

- Decision: the DAO is tested against real Room/SQLite with Robolectric (SDK 34) in the normal
  `testDebugUnitTest` run; repository and ViewModels are tested with in-memory fakes.
- Why: CI has no emulator, so instrumented tests cannot run there. Trade-off: not real-device SQLite (KI-013).

## D-014 — Validation rules for tasks (2026-11-17, Phase 3)

- Decision: titles are trimmed, must not be blank, and are capped at 120 characters. Validation
  lives in `TaskTitle` (domain), used by the ViewModel for UI feedback and by the repository as a guard.
- Deleting a task asks for confirmation; storage failures show a dismissible message, not a crash.

## D-015 — Prayer engine: adhan2 0.0.5 (2026-11-18, Phase 4)

- Decision: use adhan2 (MIT) pinned to 0.0.5. 0.0.7 needs Kotlin 2.4 metadata and 0.0.6 needs Java 21
  bytecode; both failed in CI against Kotlin 2.1.21 / JDK 17. Adjustments are applied once in our
  adapter, never in the library. Revisit when the toolchain is upgraded.

## D-016 — Prayer settings in DataStore; prayer logs in Room (2026-11-18)

- Settings are small key/value data, observed as a Flow: DataStore Preferences. Logs are relational
  and queried by date: Room. Database version 2 via explicit `MIGRATION_1_2`; schema export is still
  off (KI-010).

## D-017 — Coarse foreground location with the framework LocationManager (2026-11-18)

- Only `ACCESS_COARSE_LOCATION`, one lookup on button tap. No Play Services, no geocoder, no
  background location, no network. Trade-off: automatic fixes have no city name.

## D-018 — Alarms: AlarmManager; WorkManager only for maintenance (2026-11-18)

- Alerts are `AlarmManager` alarms (exact when `canScheduleExactAlarms()`, otherwise inexact with an
  honest UI note). WorkManager re-plans every 12 hours but never delivers alerts. Rolling window of
  today and tomorrow, stable request codes, cancel-then-schedule to avoid duplicates.

## D-019 — Prayer day and tracking date (2026-11-18)

- The prayer day is the local date in the location's IANA time zone, re-derived on every tick. Logs
  store it as `epochDay`. A passed prayer without a log is "unrecorded", never "missed".

## D-020 — Hijri date source (2026-11-18)

- `java.time` `HijrahChronology` (Umm al-Qura) with own Arabic month names and a ±2 day user offset;
  hidden outside the supported range. It may differ by a day from local sighting.

## D-021 — Sound: default and silent only (2026-11-18)

- Two channels (`prayer_sound`, `prayer_silent`). Adhan audio is a later, separately licensed
  enhancement (documented in `PRAYER_SYSTEM.md`).

## D-022 — Phase 4 stacked on unmerged Phase 3 (2026-11-18)

- The Phase 4 spec says to stop if Phase 3 is unmerged. The owner explicitly approved ("موافق") a
  stacked branch: `feature/prayer-system` -> `feature/tasks-persistence` -> `feature/android-foundation`.
  Merge order: #2, #3, then retarget Phase 4 to `main`.

## D-023 — Adhkar text is content, not code (2026-11-18, Phase 5)

- Decision: adhkar texts live in `assets/adhkar.json`, parsed strictly (unique ids, known category,
  non-blank text, repeat >= 1; one bad entry rejects the file). No text is written from memory or
  copied from sources whose licence is unclear: islamweb.net asserts "all rights reserved", and
  public JSON datasets found had no stated licence. The shipped file is empty until a reviewed,
  licensed source is supplied.

## D-024 — Adhkar counters per local day in Room (2026-11-18)

- One row per (epochDay, category, dhikrId); database version 3 with `MIGRATION_2_3`. A new day
  starts at zero because there is no row for it; nothing is deleted.

## D-025 — Phase 5 stacked on the unmerged chain (2026-11-18)

- `feature/adhkar` -> `feature/prayer-system` -> `feature/tasks-persistence` -> `feature/android-foundation`.
  The owner asked to proceed; merge order is #2, #3, #4, then this PR.
