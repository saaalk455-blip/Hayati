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
| KI-006 | Low | `android-build.yml` uses `actions/setup-java@v4` and other actions that GitHub warns are on deprecated Node 20 | Warnings today; may need action upgrades later | Open |
| KI-007 | Low | Custom icons are hand-written paths; owner reported Phase 2 works, but icon polish was not specifically reported | Possible visual differences from intent | Open — confirm with owner |
| KI-008 | Low | UI is forced RTL/Arabic regardless of device language | Non-Arabic users see Arabic only | Accepted for now (D-008) |
| KI-009 | Low | No Compose UI tests or lint in CI | UI regressions are only caught by manual testing | Open |
