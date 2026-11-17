package com.hayati.app.domain.prayer.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Calculated (and already adjusted) times for one local date at one location. */
data class PrayerTimes(
    val date: LocalDate,
    val zone: ZoneId,
    val times: Map<PrayerEvent, Instant>,
) {
    fun time(event: PrayerEvent): Instant = times.getValue(event)

    fun time(prayer: Prayer): Instant = time(PrayerEvent.of(prayer))
}

data class NextPrayer(
    val prayer: Prayer,
    val time: Instant,
    /** True when today's prayers are all over and this is tomorrow's Fajr. */
    val isTomorrow: Boolean,
)

class PrayerCalculationException(message: String, cause: Throwable? = null) : Exception(message, cause)
