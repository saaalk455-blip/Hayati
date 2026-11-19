package com.hayati.app.data.adhkar.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AdhkarProgressDao {
    @Query("SELECT * FROM adhkar_progress WHERE progressDate = :epochDay AND category = :category")
    abstract fun observe(epochDay: Long, category: String): Flow<List<AdhkarProgressEntity>>

    @Query(
        "SELECT * FROM adhkar_progress WHERE progressDate = :epochDay AND category = :category " +
            "AND dhikrId = :dhikrId LIMIT 1",
    )
    abstract suspend fun find(epochDay: Long, category: String, dhikrId: String): AdhkarProgressEntity?

    @Insert
    abstract suspend fun insert(entity: AdhkarProgressEntity): Long

    @Update
    abstract suspend fun update(entity: AdhkarProgressEntity)

    /** Insert-or-update in one transaction so the unique key is never duplicated. */
    @Transaction
    open suspend fun setCount(epochDay: Long, category: String, dhikrId: String, count: Int, nowMillis: Long) {
        val existing = find(epochDay, category, dhikrId)
        if (existing == null) {
            if (count > 0) {
                insert(AdhkarProgressEntity(0, epochDay, category, dhikrId, count, nowMillis))
            }
        } else {
            update(existing.copy(count = count, updatedAt = nowMillis))
        }
    }
}
