# Architectural & Process Decisions

## D-001 — Adopt the permanent collaboration protocol (2026-11-16)

- Status: Accepted (provided by the owner)
- Decision: Work follows `CLAUDE.md`: feature branches, no pushes of unfinished work to `main`,
  no force pushes, mandatory final report, documentation updated after every task.
- Impact: All future tasks.

## D-002 — Target architecture (2026-11-16)

- Status: Accepted (stated in the protocol)
- Decision: Kotlin, Jetpack Compose, MVVM, Clean Architecture, SOLID, dependency injection,
  lifecycle-aware state, RTL-ready layouts, real persistent data for completed features.
- Open: concrete libraries (DI framework, database, navigation) are **not decided** and will be
  recorded here when the bootstrap specification is approved.

## D-003 — Public repository hygiene (2026-11-16)

- Status: Accepted
- Decision: `.gitignore` excludes `local.properties`, keystores, signing configs, `google-services.json`,
  `.env` files and build outputs. Secrets are never committed.
