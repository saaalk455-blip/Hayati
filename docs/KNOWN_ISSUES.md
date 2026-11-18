# Known Issues

| ID | Severity | Issue | Impact | Status |
|---|---|---|---|---|
| KI-001 | Medium | No Android SDK or Maven access in the Claude authoring environment | Builds/tests run only in GitHub Actions, so each check costs a push and a CI wait | Open (by design, see D-007) |
| KI-004 | Medium | The Phase 3 (tasks) build was never installed or run on a device by Claude | Keyboard/IME behavior, dialogs, RTL text entry, scrolling and the look of the Tasks screen are unverified | Open — needs owner device test. (The Phase 2 build was owner-tested and reported working.) |
| KI-005 | Medium | Debug APK is debug-signed | Play Protect may warn on install; not suitable for distribution | Open — release signing is a later phase |
| KI-010 | Medium | Room schema export is off (`exportSchema = false`) | The first schema change cannot be migration-tested automatically | Open — enable export and commit schema JSON before changing the schema |
| KI-011 | Low | Tasks belong to one calendar day; unfinished tasks are not carried to the next day and there is no date navigation | Yesterday's open tasks are stored but not visible in the UI | Open — product decision needed |
| KI-012 | Low | Home may show "غير متاح بعد" for the tasks item for a moment before the first database read | Brief visual flicker on launch | Open — verify on device |
| KI-013 | Low | Database tests run on Robolectric pinned to Android SDK 34 | Real-device SQLite differences are not covered | Accepted |
| KI-014 | High | Phase 4 was never installed or run on a device by Claude | Location flow, permission dialogs, notification appearance/sound, alarm delivery, RTL layout of the Prayer screens are unverified | Open — needs owner device test |
| KI-015 | High | Prayer times were not compared with an official timetable; tests use physical invariants only | A wrong time for a given city/method would not be caught | Open — owner to compare with a trusted local timetable and report differences |
| KI-016 | Medium | Notification delivery is not guaranteed: Doze, battery optimisation, OEM task killers, revoked notification or exact-alarm permission | Alerts may be late or missing | Open — verify on device; documented in `PRAYER_SYSTEM.md` |
| KI-017 | Low | Hijri date uses Umm al-Qura calendar data, can differ by one day from local sighting | Possible ±1 day; the offset setting is the workaround | Accepted |
| KI-018 | Medium | No high-latitude rule configured; Isha/Fajr can be unusual or after midnight at high latitudes | Odd times for far-north/south locations | Open — product decision |
| KI-019 | Low | No Adhan audio; only default and silent sounds | Expected limit of Phase 4 | Planned enhancement |
| KI-020 | Low | Automatic location has no city name (no geocoder); only coordinates are shown | Less friendly header | Accepted (D-017) |
| KI-021 | Low | adhan2 pinned to 0.0.5 because newer versions need Kotlin 2.4 / Java 21 | Misses newer library fixes | Open — revisit on toolchain upgrade |
| KI-022 | Low | WorkManager adds WAKE_LOCK, ACCESS_NETWORK_STATE and FOREGROUND_SERVICE permissions to the merged manifest | Extra permission lines in the APK though the app has no internet or foreground service | Accepted |
| KI-023 | Medium | Stacked PR chain (#2 <- #3 <- Phase 4) | Merge order matters; the Phase 4 PR must be retargeted to `main` after #3 merges | Open — owner action |
| KI-024 | Low | Phase 4 artifact is named `hayati-debug.apk`, not the suggested `hayati-phase-4-prayer-debug.apk` | Cosmetic; version is visible in Settings and `aapt2` | Accepted |
| KI-025 | High | No adhkar text is shipped; `assets/adhkar.json` is empty | The Adhkar screen shows "content not available yet"; the feature is not usable | Open — needs reviewed, licensed text (owner/ChatGPT to supply, or written permission from a publisher) |
| KI-026 | Low | Home "أذكار اليوم" still shows "غير متاح بعد" | Home does not reflect adhkar progress | Open — deferred until content exists |
| KI-027 | Low | Phase 5 was never run on a device by Claude | Layout of long adhkar text, tap behavior and RTL are unverified | Open — needs owner device test |
| KI-028 | Low | No adhkar reminders/notifications | Expected scope limit | Planned |
| KI-006 | Low | `android-build.yml` uses `actions/setup-java@v4` and other actions that GitHub warns are on deprecated Node 20 | Warnings today; may need action upgrades later | Open |
| KI-007 | Low | Custom icons are hand-written paths; owner reported Phase 2 works, but icon polish was not specifically reported | Possible visual differences from intent | Open — confirm with owner |
| KI-008 | Low | UI is forced RTL/Arabic regardless of device language | Non-Arabic users see Arabic only | Accepted for now (D-008) |
| KI-009 | Low | No lint in CI (Compose UI tests exist for the Prayer screen only, via Robolectric) | Other screens and real-device rendering are not covered | Open |
