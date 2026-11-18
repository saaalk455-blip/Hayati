# Prayer System (Phase 4, version 0.4.0)

Status: implemented, CI-verified (137 tests). **Not device-tested. Not compared with an official
timetable.** See `docs/KNOWN_ISSUES.md`.

## Calculation engine
- Library: **adhan2** `com.batoulapps.adhan:adhan2:0.0.5`, licence **MIT**.
- Why 0.0.5: 0.0.7 is compiled with Kotlin 2.4 metadata and 0.0.6 with Java 21 bytecode; neither
  works with this project's Kotlin 2.1.21 / JDK 17 toolchain (found through CI). 0.0.5 pulls
  kotlinx-datetime 0.5.0. Upgrade when the toolchain moves (KI-021).
- Methods (10): Egyptian, Muslim World League, Umm al-Qura, North America (ISNA), Karachi, Dubai, Qatar,
  Kuwait, Turkey, Singapore. Asr: Standard (Shafi'i) and Hanafi.
- Per-prayer minute adjustments (±30) are applied **once**, in `AdhanPrayerTimeCalculator`; the
  library's own adjustments stay at zero (tested).
- Calculation is fully offline. Default method: Egyptian if the device region is EG, otherwise MWL;
  a chosen city's recommended method applies only while the user has not chosen a method.
- High-latitude rule is not configured (KI-018).

## Time and day rules
- Times are computed for the date in the location's own IANA time zone, so DST is handled by
  `java.time` zone rules. The prayer day is re-derived from the clock on every tick, so midnight and
  clock/timezone changes move the day correctly.
- After Isha the "next" prayer is tomorrow's Fajr (computed for tomorrow).

## Location and privacy
- Manual: country, city (offline list of 34 cities in `KnownCities`, or free text), latitude,
  longitude, time zone; validated (Arabic digits and commas accepted). A manual location is never
  silently replaced.
- Automatic: `ACCESS_COARSE_LOCATION` only (foreground), one lookup when the user taps the button,
  via the framework `LocationManager`. No background location, no network upload, no geocoder, so an
  automatic fix has no city name; the UI shows "موقعك الحالي" and coordinates.
- Permission denial leaves the app usable with manual location.

## Hijri date
Java `HijrahChronology` (Umm al-Qura) with own Arabic month names and a user offset of ±2 days.
Returns nothing outside the supported range (the date is hidden, never guessed). The calendar can
differ by a day from local moon sighting (KI-017).

## Tracking
Room table `prayer_logs` (see `DATABASE_SCHEMA.md`). The user marks a prayer done; nothing is
completed automatically. Visual states: upcoming / next / passed-unrecorded / completed. Daily
summary counts only the five prayers (sunrise excluded). A history screen lists previous days.

## Notifications
- Settings per prayer: enabled, at-time alert, optional reminder (0/5/10/15 min), sound
  (default or silent). Master switch ON enables all five prayers if none were enabled.
- Permission: `POST_NOTIFICATIONS` (Android 13+) requested from settings; denial is reported, not
  hidden.
- Scheduling: `AlarmManager`. `setExactAndAllowWhileIdle` only when `canScheduleExactAlarms()`;
  otherwise `setAndAllowWhileIdle` (may be late by minutes; the UI explains this and links to the
  system exact-alarm screen). Stable request code = `epochDay*100 + prayer*10 + kind`.
- Rolling window: today and tomorrow. Everything from yesterday to +2 days is cancelled first, so a
  reschedule never duplicates alarms.
- Reschedule triggers: location, method, Asr, adjustments, notification settings, boot, timezone
  change, time change, app update, exact-alarm permission change; plus a 12-hour WorkManager
  maintenance job (maintenance only, never the alert itself).
- Late handling: an at-time alert more than 10 minutes late uses "late" wording; a reminder whose
  prayer time has passed is dropped.
- Channels: `prayer_sound` (default sound) and `prayer_silent`. Content is Arabic.
- Delivery is **not guaranteed**: battery optimisation, OEM task killers, Doze and revoked
  permissions can delay or block alarms (KI-016).
- Adhan audio is not included. Planned later enhancement: bundled, licensed Adhan audio selectable
  per prayer on a dedicated channel.

## Settings storage
DataStore Preferences. Home and the Prayer screen read one `PrayerSnapshotProvider`, so they cannot
disagree.

## Tests (CI run 44222022199)
Calculation: invariants rather than copied timetables (Dhuhr within 20 min of solar noon; Mecca
equinox day length about 12 h; Hanafi Asr 30–90 min later; Egyptian Fajr earlier than MWL; an
adjustment of +N moves exactly N min; London DST shift about 60 min; Hijri anchor
2025-06-26 = 1 محرم 1447). Tracking: Room DAO, ViewModels. Notifications: coordinator plus
Robolectric AlarmManager/NotificationManager shadows. UI: Compose tests on Robolectric.

## Known limitations
Isha after midnight at high latitudes, no high-latitude rule, Hijri ±1 day, no Adhan audio, no
official-timetable comparison, no device test. See `docs/KNOWN_ISSUES.md`.
