package com.hayati.app.domain.prayer.usecase

import com.hayati.app.domain.prayer.model.AlertKind
import com.hayati.app.domain.prayer.model.NotificationSettings
import com.hayati.app.domain.prayer.model.PlannedAlert
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerTimes
import java.time.LocalDate

/** Pure planning of which alerts to schedule. No Android types, so it is fully unit-testable. */
object AlertPlanner {
    /** Same (date, prayer, kind) always gives the same code, so re-scheduling replaces, never duplicates. */
    fun requestCode(date: LocalDate, prayer: Prayer, kind: AlertKind): Int =
        (date.toEpochDay().toInt() * 100) + prayer.ordinal * 10 + kind.code

    fun allCodesFor(date: LocalDate): List<Int> =
        Prayer.entries.flatMap { prayer -> AlertKind.entries.map { requestCode(date, prayer, it) } }

    /**
     * Alerts for the given days that are still in the future. Only prayers with an active
     * notification setting are planned; the master switch off means nothing is planned.
     */
    fun plan(
        nowMillis: Long,
        days: List<PrayerTimes>,
        settings: NotificationSettings,
        use24Hour: Boolean,
    ): List<PlannedAlert> {
        if (!settings.enabled) return emptyList()
        return days.flatMap { day ->
            Prayer.entries.filter { settings.isActive(it) }.flatMap { prayer ->
                val config = settings.config(prayer)
                val prayerMillis = day.time(prayer).toEpochMilli()
                fun alert(kind: AlertKind, at: Long) = PlannedAlert(
                    requestCode = requestCode(day.date, prayer, kind),
                    triggerAtMillis = at,
                    prayer = prayer,
                    kind = kind,
                    reminderMinutes = config.reminderMinutes,
                    prayerTimeMillis = prayerMillis,
                    sound = config.sound,
                    zoneId = day.zone.id,
                    use24Hour = use24Hour,
                )
                buildList {
                    add(alert(AlertKind.AT_TIME, prayerMillis))
                    if (config.reminderMinutes > 0) {
                        add(alert(AlertKind.REMINDER, prayerMillis - config.reminderMinutes * 60_000L))
                    }
                }
            }
        }.filter { it.triggerAtMillis > nowMillis }
    }
}
