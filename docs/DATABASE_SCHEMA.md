# Database Schema

Room database `hayati.db`, **version 2**. `exportSchema = false` (KI-010): no schema JSON is
committed, so migrations are verified by a hand-written test, not by Room's migration test helper.

## Table `tasks` (version 1, Phase 3)

| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK autoincrement | |
| title | TEXT NOT NULL | trimmed, 1–120 chars (enforced in domain) |
| isCompleted | INTEGER NOT NULL | boolean |
| epochDay | INTEGER NOT NULL | calendar day of the task; indexed (`index_tasks_epochDay`) |
| createdAtMillis | INTEGER NOT NULL | |
| completedAtMillis | INTEGER | null while open |

## Table `prayer_logs` (added in version 2, Phase 4)

| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK autoincrement | |
| prayerDate | INTEGER NOT NULL | local date **at the prayer location**, as days since 1970-01-01 (a plain number, so time-zone conversion cannot shift it) |
| prayerType | TEXT NOT NULL | `FAJR`, `DHUHR`, `ASR`, `MAGHRIB`, `ISHA` (sunrise is never stored) |
| status | TEXT NOT NULL | `COMPLETED` or `NOT_COMPLETED` |
| completedAt | INTEGER | epoch millis, null when not completed |
| createdAt | INTEGER NOT NULL | |
| updatedAt | INTEGER NOT NULL | |

Unique index `index_prayer_logs_prayerDate_prayerType` on (`prayerDate`, `prayerType`): at most one
row per prayer per day.

Rules: a row is only written when the user marks a prayer. A prayer with no row is **unrecorded**
(never "missed"). Reverting a prayer that has no row writes nothing. Rows are never created
automatically.

## Migration 1 → 2

`MIGRATION_1_2` creates `prayer_logs` and its unique index. `tasks` is untouched. Covered by
`DatabaseMigrationTest` (creates a v1 database with a task, migrates, checks the task survived and
`prayer_logs` works). No destructive fallback is configured.

## Not in the database

Prayer settings (method, Asr, adjustments, location, notification settings) live in DataStore
Preferences (`prayer_settings`), not Room. See `docs/PRAYER_SYSTEM.md`.
