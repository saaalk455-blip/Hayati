package com.hayati.app.domain.prayer.usecase

import com.hayati.app.domain.prayer.model.NextPrayer
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerRow
import com.hayati.app.domain.prayer.model.PrayerRowState
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerTimes
import java.time.Instant

/** Pure functions: the same input always gives the same answer, so they are easy to test. */
object NextPrayerResolver {
    /**
     * The first obligatory prayer strictly after [now]. Sunrise is never returned. After today's
     * Isha it is tomorrow's Fajr.
     */
    fun resolve(now: Instant, today: PrayerTimes, tomorrow: PrayerTimes): NextPrayer {
        Prayer.entries.forEach { prayer ->
            val time = today.time(prayer)
            if (time.isAfter(now)) return NextPrayer(prayer, time, isTomorrow = false)
        }
        return NextPrayer(Prayer.FAJR, tomorrow.time(Prayer.FAJR), isTomorrow = true)
    }

    fun rowState(
        event: PrayerEvent,
        time: Instant,
        now: Instant,
        next: NextPrayer?,
        completed: Boolean,
    ): PrayerRowState {
        if (event.prayer == null) {
            return if (now.isBefore(time)) PrayerRowState.UPCOMING else PrayerRowState.PASSED
        }
        return when {
            completed -> PrayerRowState.COMPLETED
            next != null && !next.isTomorrow && next.prayer == event.prayer -> PrayerRowState.NEXT
            // A passed prayer without a record is "unrecorded", never assumed missed.
            now.isBefore(time) -> PrayerRowState.UPCOMING
            else -> PrayerRowState.UNRECORDED
        }
    }

    fun rows(
        now: Instant,
        today: PrayerTimes,
        next: NextPrayer?,
        completedPrayers: Set<Prayer>,
        notificationActive: (Prayer) -> Boolean,
    ): List<PrayerRow> = PrayerEvent.entries.map { event ->
        val time = today.time(event)
        val prayer = event.prayer
        PrayerRow(
            event = event,
            time = time,
            state = rowState(event, time, now, next, prayer != null && prayer in completedPrayers),
            completed = prayer?.let { it in completedPrayers },
            notificationActive = prayer?.let(notificationActive),
        )
    }
}
