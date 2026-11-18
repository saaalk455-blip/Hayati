package com.hayati.app.domain.adhkar.repository

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import kotlinx.coroutines.flow.Flow

/** Where the adhkar text comes from. Throws [AdhkarContentException] when the file is invalid. */
interface AdhkarContentSource {
    suspend fun load(): List<Dhikr>
}

class AdhkarContentException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Counter per (day, category, dhikr). Days are `epochDay` in the device's local time zone. */
interface AdhkarProgressRepository {
    /** dhikr id -> count for that day and category. Missing ids mean 0. */
    fun observeCounts(epochDay: Long, category: AdhkarCategory): Flow<Map<String, Int>>

    /** Stores [count] (already clamped by the caller). */
    suspend fun setCount(epochDay: Long, category: AdhkarCategory, dhikrId: String, count: Int)
}
