package com.hayati.app.domain.prayer.model

/** A prayer the user explicitly recorded for a local prayer date. */
data class PrayerLog(
    val id: Long,
    /** Local date at the prayer location, as days since 1970-01-01. */
    val epochDay: Long,
    val prayer: Prayer,
    val completed: Boolean,
    val completedAtMillis: Long?,
)

/** Only the five obligatory prayers are counted. */
data class DailySummary(val completed: Int, val remaining: Int, val percent: Int) {
    companion object {
        fun from(logs: List<PrayerLog>): DailySummary {
            val done = logs.filter { it.completed }.map { it.prayer }.toSet().size
            val total = Prayer.entries.size
            return DailySummary(completed = done, remaining = total - done, percent = done * 100 / total)
        }
    }
}
