package com.hayati.app.domain.prayer.usecase

import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLog

/** A day that has at least one prayer the user recorded. */
data class HistoryDay(val epochDay: Long, val completed: Set<Prayer>) {
    val count: Int get() = completed.size
}

object HistoryBuilder {
    /** Only days with a recorded (completed) prayer appear; nothing is inferred for other days. */
    fun build(logs: List<PrayerLog>): List<HistoryDay> = logs
        .filter { it.completed }
        .groupBy { it.epochDay }
        .map { (day, rows) -> HistoryDay(day, rows.map { it.prayer }.toSet()) }
        .sortedByDescending { it.epochDay }
}
