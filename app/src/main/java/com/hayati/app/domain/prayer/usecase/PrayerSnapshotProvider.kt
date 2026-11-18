package com.hayati.app.domain.prayer.usecase

import com.hayati.app.domain.prayer.model.PrayerCalculationException
import com.hayati.app.domain.prayer.model.PrayerErrorReason
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.model.PrayerTimes
import com.hayati.app.domain.prayer.repository.PrayerLogRepository
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import com.hayati.app.domain.prayer.repository.PrayerTimeCalculator
import com.hayati.app.domain.prayer.model.DailySummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Single source of truth for prayer information shown on Home and the Prayer screen.
 *
 * Prayer day = the calendar date in the prayer location's own time zone (it changes at local
 * midnight). The date is re-derived from the clock on every tick, so date, time zone and device
 * clock changes are picked up. Times are recalculated only when the date or settings change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PrayerSnapshotProvider(
    private val settingsRepository: PrayerSettingsRepository,
    private val logRepository: PrayerLogRepository,
    private val calculator: PrayerTimeCalculator,
) {
    /** [ticker] emits the current time in epoch millis; it should be shared by the caller. */
    fun observe(ticker: Flow<Long>): Flow<PrayerSnapshot> =
        settingsRepository.settings.flatMapLatest { settings -> observe(settings, ticker) }

    private fun observe(settings: PrayerSettings, ticker: Flow<Long>): Flow<PrayerSnapshot> {
        val location = settings.location ?: return flow { emit(PrayerSnapshot.NotConfigured) }
        val zone = zoneOf(location) ?: return flow { emit(PrayerSnapshot.Error(PrayerErrorReason.INVALID_LOCATION)) }

        return ticker
            .map { now -> Instant.ofEpochMilli(now).atZone(zone).toLocalDate() }
            .distinctUntilChanged()
            .flatMapLatest { date -> observeDay(settings, location, zone, date, ticker) }
    }

    private fun observeDay(
        settings: PrayerSettings,
        location: PrayerLocation,
        zone: ZoneId,
        date: LocalDate,
        ticker: Flow<Long>,
    ): Flow<PrayerSnapshot> = flow {
        val times = try {
            calculator.calculate(location, date, settings.calculation) to
                calculator.calculate(location, date.plusDays(1), settings.calculation)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: PrayerCalculationException) {
            emit(PrayerSnapshot.Error(PrayerErrorReason.CALCULATION_FAILED))
            return@flow
        }
        emitAll(
            combine(logRepository.observeDay(date.toEpochDay()), ticker) { logs, now ->
                build(settings, location, zone, date, times.first, times.second, logs, now)
            },
        )
    }

    private fun build(
        settings: PrayerSettings,
        location: PrayerLocation,
        zone: ZoneId,
        date: LocalDate,
        today: PrayerTimes,
        tomorrow: PrayerTimes,
        logs: List<com.hayati.app.domain.prayer.model.PrayerLog>,
        nowMillis: Long,
    ): PrayerSnapshot {
        val now = Instant.ofEpochMilli(nowMillis)
        val next = NextPrayerResolver.resolve(now, today, tomorrow)
        val completed = logs.filter { it.completed }.map { it.prayer }.toSet()
        return PrayerSnapshot.Ready(
            location = location,
            date = date,
            zone = zone,
            nowMillis = nowMillis,
            rows = NextPrayerResolver.rows(now, today, next, completed) { settings.notifications.isActive(it) },
            next = next,
            summary = DailySummary.from(logs),
            use24Hour = settings.use24Hour,
            hijriOffsetDays = settings.hijriOffsetDays,
        )
    }

    private fun zoneOf(location: PrayerLocation): ZoneId? =
        if (PrayerLocation.isValidZone(location.zoneId)) location.zone else null
}
