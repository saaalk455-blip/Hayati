package com.hayati.app.data.prayer.calculator

import com.batoulapps.adhan2.CalculationMethod as AdhanMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes as AdhanPrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerCalculationException
import com.hayati.app.domain.prayer.model.PrayerCalculationSettings
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerTimes
import com.hayati.app.domain.prayer.repository.PrayerTimeCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import kotlin.time.ExperimentalTime

/**
 * Prayer times from the open-source adhan2 library (MIT, com.batoulapps.adhan:adhan2). This is
 * the only class that touches the library, so the engine can be replaced without touching the app.
 *
 * Manual minute adjustments are applied here, once, on the returned instants. The library's own
 * adjustment feature is left at zero so an adjustment can never be applied twice.
 */
@OptIn(ExperimentalTime::class) // adhan2 exposes kotlin.time.Instant, which is still experimental
class AdhanPrayerTimeCalculator(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PrayerTimeCalculator {

    override suspend fun calculate(
        location: PrayerLocation,
        date: LocalDate,
        settings: PrayerCalculationSettings,
    ): PrayerTimes = withContext(dispatcher) {
        if (!PrayerLocation.isValidLatitude(location.latitude) ||
            !PrayerLocation.isValidLongitude(location.longitude)
        ) {
            throw PrayerCalculationException("Invalid coordinates")
        }
        try {
            val parameters = settings.method.toAdhan().parameters.copy(
                madhab = when (settings.asr) {
                    AsrMethod.STANDARD -> Madhab.SHAFI
                    AsrMethod.HANAFI -> Madhab.HANAFI
                },
            )
            val raw = AdhanPrayerTimes(
                Coordinates(location.latitude, location.longitude),
                DateComponents(date.year, date.monthValue, date.dayOfMonth),
                parameters,
            )
            fun at(millis: Long, prayer: Prayer?): Instant {
                val base = Instant.ofEpochMilli(millis)
                return if (prayer == null) base else base.plusSeconds(settings.adjustments.of(prayer) * 60L)
            }
            PrayerTimes(
                date = date,
                zone = location.zone,
                times = mapOf(
                    PrayerEvent.FAJR to at(raw.fajr.toEpochMilliseconds(), Prayer.FAJR),
                    PrayerEvent.SUNRISE to at(raw.sunrise.toEpochMilliseconds(), null),
                    PrayerEvent.DHUHR to at(raw.dhuhr.toEpochMilliseconds(), Prayer.DHUHR),
                    PrayerEvent.ASR to at(raw.asr.toEpochMilliseconds(), Prayer.ASR),
                    PrayerEvent.MAGHRIB to at(raw.maghrib.toEpochMilliseconds(), Prayer.MAGHRIB),
                    PrayerEvent.ISHA to at(raw.isha.toEpochMilliseconds(), Prayer.ISHA),
                ),
            )
        } catch (failure: PrayerCalculationException) {
            throw failure
        } catch (failure: RuntimeException) {
            throw PrayerCalculationException("Calculation failed", failure)
        }
    }

    private fun CalculationMethod.toAdhan(): AdhanMethod = when (this) {
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> AdhanMethod.MUSLIM_WORLD_LEAGUE
        CalculationMethod.EGYPTIAN -> AdhanMethod.EGYPTIAN
        CalculationMethod.UMM_AL_QURA -> AdhanMethod.UMM_AL_QURA
        CalculationMethod.NORTH_AMERICA -> AdhanMethod.NORTH_AMERICA
        CalculationMethod.KARACHI -> AdhanMethod.KARACHI
        CalculationMethod.DUBAI -> AdhanMethod.DUBAI
        CalculationMethod.QATAR -> AdhanMethod.QATAR
        CalculationMethod.KUWAIT -> AdhanMethod.KUWAIT
        CalculationMethod.TURKEY -> AdhanMethod.TURKEY
        CalculationMethod.SINGAPORE -> AdhanMethod.SINGAPORE
    }
}
