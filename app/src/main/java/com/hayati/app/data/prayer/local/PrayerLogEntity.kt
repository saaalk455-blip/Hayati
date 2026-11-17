package com.hayati.app.data.prayer.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLog

/**
 * One row per prayer per local prayer date. [prayerDate] is the date at the prayer location as
 * days since 1970-01-01, a plain number, so no time zone conversion can shift it.
 */
@Entity(
    tableName = "prayer_logs",
    indices = [Index(value = ["prayerDate", "prayerType"], unique = true)],
)
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerDate: Long,
    val prayerType: String,
    val status: String,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_NOT_COMPLETED = "NOT_COMPLETED"
    }
}

fun PrayerLogEntity.toDomain(): PrayerLog? {
    val prayer = Prayer.entries.firstOrNull { it.name == prayerType } ?: return null
    return PrayerLog(
        id = id,
        epochDay = prayerDate,
        prayer = prayer,
        completed = status == PrayerLogEntity.STATUS_COMPLETED,
        completedAtMillis = completedAt,
    )
}
