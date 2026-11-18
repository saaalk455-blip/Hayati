package com.hayati.app.data.prayer.repository

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.prayer.local.PrayerLogDao
import com.hayati.app.data.prayer.local.toDomain
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLog
import com.hayati.app.domain.prayer.repository.PrayerLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrayerLogRepositoryImpl(
    private val dao: PrayerLogDao,
    private val time: TimeProvider,
) : PrayerLogRepository {

    override fun observeDay(epochDay: Long): Flow<List<PrayerLog>> =
        dao.observeDay(epochDay).map { rows -> rows.mapNotNull { it.toDomain() } }

    override fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<PrayerLog>> =
        dao.observeRange(fromEpochDay, toEpochDay).map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun setCompleted(epochDay: Long, prayer: Prayer, completed: Boolean) {
        dao.setStatus(epochDay, prayer.name, completed, time.nowMillis())
    }
}
