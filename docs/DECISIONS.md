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
