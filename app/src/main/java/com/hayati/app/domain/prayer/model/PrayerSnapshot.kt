package com.hayati.app.domain.prayer.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class PrayerRowState { UPCOMING, NEXT, PASSED, COMPLETED, UNRECORDED }

data class PrayerRow(
    val event: PrayerEvent,
    val time: Instant,
    val state: PrayerRowState,
    /** null for sunrise, which is not a prayer and has no tracking. */
    val completed: Boolean?,
    /** null for sunrise; otherwise whether a notification is active for this prayer. */
    val notificationActive: Boolean?,
)

enum class PrayerErrorReason { INVALID_LOCATION, CALCULATION_FAILED }

/** What every screen shows. One provider builds it, so Home and the Prayer screen never disagree. */
sealed interface PrayerSnapshot {
    data object Loading : PrayerSnapshot

    data object NotConfigured : PrayerSnapshot

    data class Error(val reason: PrayerErrorReason) : PrayerSnapshot

    data class Ready(
        val location: PrayerLocation,
        val date: LocalDate,
        val zone: ZoneId,
        val nowMillis: Long,
        val rows: List<PrayerRow>,
        val next: NextPrayer?,
        val summary: DailySummary,
        val use24Hour: Boolean,
        val hijriOffsetDays: Int,
    ) : PrayerSnapshot {
        /** Milliseconds left until [next], never negative. */
        val remainingMillis: Long?
            get() = next?.let { (it.time.toEpochMilli() - nowMillis).coerceAtLeast(0L) }
    }
}
