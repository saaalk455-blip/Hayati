package com.hayati.app.domain.prayer.repository

import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerCalculationSettings
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerLog
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.model.PrayerTimes
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Computes prayer times locally; never uses the network. */
interface PrayerTimeCalculator {
    /** Returns times already including the user's minute adjustments. */
    suspend fun calculate(
        location: PrayerLocation,
        date: LocalDate,
        settings: PrayerCalculationSettings,
    ): PrayerTimes
}

interface PrayerSettingsRepository {
    val settings: Flow<PrayerSettings>

    /** Applies [transform] to the stored settings atomically. No write happens if nothing changed. */
    suspend fun update(transform: (PrayerSettings) -> PrayerSettings)
}

interface PrayerLogRepository {
    fun observeDay(epochDay: Long): Flow<List<PrayerLog>>

    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<PrayerLog>>

    /** Records or reverts a prayer. Creates no row when reverting a prayer that has no record. */
    suspend fun setCompleted(epochDay: Long, prayer: Prayer, completed: Boolean)
}
