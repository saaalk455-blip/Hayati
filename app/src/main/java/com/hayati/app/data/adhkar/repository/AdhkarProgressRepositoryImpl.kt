package com.hayati.app.data.adhkar.repository

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.adhkar.local.AdhkarProgressDao
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.repository.AdhkarProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AdhkarProgressRepositoryImpl(
    private val dao: AdhkarProgressDao,
    private val time: TimeProvider,
) : AdhkarProgressRepository {

    override fun observeCounts(epochDay: Long, category: AdhkarCategory): Flow<Map<String, Int>> =
        dao.observe(epochDay, category.key).map { rows -> rows.associate { it.dhikrId to it.count } }

    override suspend fun setCount(epochDay: Long, category: AdhkarCategory, dhikrId: String, count: Int) {
        dao.setCount(epochDay, category.key, dhikrId, count.coerceAtLeast(0), time.nowMillis())
    }
}
