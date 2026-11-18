package com.hayati.app.data.prayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE prayerDate = :epochDay ORDER BY id ASC")
    abstract fun observeDay(epochDay: Long): Flow<List<PrayerLogEntity>>

    @Query(
        "SELECT * FROM prayer_logs WHERE prayerDate BETWEEN :from AND :to " +
            "ORDER BY prayerDate DESC, id ASC",
    )
    abstract fun observeRange(from: Long, to: Long): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE prayerDate = :epochDay AND prayerType = :type LIMIT 1")
    abstract suspend fun find(epochDay: Long, type: String): PrayerLogEntity?

    @Insert
    abstract suspend fun insert(entity: PrayerLogEntity): Long

    @Update
    abstract suspend fun update(entity: PrayerLogEntity)

    /** Insert-or-update in one transaction, so the unique (date, type) pair is never duplicated. */
    @Transaction
    open suspend fun setStatus(
        epochDay: Long,
        type: String,
        completed: Boolean,
        nowMillis: Long,
    ) {
        val existing = find(epochDay, type)
        val status = if (completed) PrayerLogEntity.STATUS_COMPLETED else PrayerLogEntity.STATUS_NOT_COMPLETED
        val completedAt = if (completed) nowMillis else null
        when {
            existing == null && !completed -> Unit // nothing recorded, nothing to revert: no write
            existing == null -> insert(
                PrayerLogEntity(
                    prayerDate = epochDay,
                    prayerType = type,
                    status = status,
                    completedAt = completedAt,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                ),
            )
            existing.status == status -> Unit // already in the requested state: no write
            else -> update(existing.copy(status = status, completedAt = completedAt, updatedAt = nowMillis))
        }
    }
}
