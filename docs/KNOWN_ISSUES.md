# Known Issues

| ID | Severity | Issue | Impact | Status |
|---|---|---|---|---|
| KI-001 | Medium | No Android SDK or Maven access in the Claude authoring environment | Builds/tests run only in GitHub Actions, so each check costs a push and a CI wait | Open (by design, see D-007) |
| KI-004 | High | APK never installed or run on a device/emulator | Startup crashes, layout, RTL, back-button behavior and the look of custom-drawn icons/decoration are unverified | Open — needs owner device test |
| KI-005 | Medium | Debug APK is debug-signed | Play Protect may warn on install; not suitable for distribution | Open — release signing is a later phase |
| KI-006 | Low | `android-build.yml` uses `actions/setup-java@v4` and other actions that GitHub warns are on deprecated Node 20 | Warnings today; may need action upgrades later | Open |
| KI-007 | Low | Worship (crescent) and other custom icons are hand-written paths; shape quality not visually checked | Icon may look different from intent | Open — verify on device |
| KI-008 | Low | UI is forced RTL/Arabic regardless of device language | Non-Arabic users see Arabic only | Accepted for now (D-008) |
| KI-009 | Low | No Compose UI tests or lint in CI | UI regressions are only caught by manual testing | Open |
| KI-002 | Low | (resolved) `README.md` was only a title | – | Resolved in Phase 2 |
| KI-003 | Low | (resolved) No Android project existed | – | Resolved in Phase 2 |
