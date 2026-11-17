package com.hayati.app.data.prayer

import com.hayati.app.data.prayer.calculator.AdhanPrayerTimeCalculator
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerAdjustments
import com.hayati.app.domain.prayer.model.PrayerCalculationException
import com.hayati.app.domain.prayer.model.PrayerCalculationSettings
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerLocation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * These tests use physical invariants and a solar-noon reference rather than copied timetables.
 * Solar noon at longitude L is about 12:00 UTC - L/15 hours, shifted by the equation of time
 * (never more than about 17 minutes over the year), so Dhuhr must land within 20 minutes of it.
 * They do NOT prove agreement with any particular mosque timetable.
 */
class AdhanPrayerTimeCalculatorTest {
    private val calculator = AdhanPrayerTimeCalculator()
    private val cairo = PrayerLocation("القاهرة", "مصر", 30.0444, 31.2357, "Africa/Cairo", LocationSource.MANUAL)
    private val london = PrayerLocation("لندن", "UK", 51.5074, -0.1278, "Europe/London", LocationSource.MANUAL)
    private val mecca = PrayerLocation("مكة", "السعودية", 21.3891, 39.8579, "Asia/Riyadh", LocationSource.MANUAL)

    private fun settings(
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        asr: AsrMethod = AsrMethod.STANDARD,
        adjustments: PrayerAdjustments = PrayerAdjustments(),
    ) = PrayerCalculationSettings(method, asr, adjustments)

    private val date = LocalDate.of(2026, 6, 15)

    @Test
    fun `events are in chronological order and sunrise sits between fajr and dhuhr`() = runTest {
        val t = calculator.calculate(cairo, date, settings())
        val order = PrayerEvent.entries.map { t.time(it) }
        assertEquals(order.sorted(), order)
        assertEquals(6, t.times.size)
    }

    @Test
    fun `dhuhr is near solar noon for the longitude`() = runTest {
        listOf(cairo, mecca, london).forEach { place ->
            val t = calculator.calculate(place, date, settings())
            val noonUtcMinutes = 12 * 60 - place.longitude / 15.0 * 60
            val dhuhr = t.time(Prayer.DHUHR).atZone(ZoneOffset.UTC)
            val dhuhrMinutes = dhuhr.hour * 60 + dhuhr.minute
            assertTrue("${place.name}: $dhuhrMinutes vs $noonUtcMinutes", kotlin.math.abs(dhuhrMinutes - noonUtcMinutes) <= 20)
        }
    }

    @Test
    fun `day length near the equinox is about twelve hours in Mecca`() = runTest {
        val t = calculator.calculate(mecca, LocalDate.of(2026, 3, 20), settings(CalculationMethod.UMM_AL_QURA))
        val hours = Duration.between(t.time(PrayerEvent.SUNRISE), t.time(Prayer.MAGHRIB)).toMinutes() / 60.0
        assertTrue("day length $hours", hours in 11.8..12.4)
    }

    @Test
    fun `hanafi asr is later than standard asr and the other prayers are unchanged`() = runTest {
        val standard = calculator.calculate(cairo, date, settings(asr = AsrMethod.STANDARD))
        val hanafi = calculator.calculate(cairo, date, settings(asr = AsrMethod.HANAFI))
        val delta = Duration.between(standard.time(Prayer.ASR), hanafi.time(Prayer.ASR)).toMinutes()
        assertTrue("asr delta $delta", delta in 30..90)
        Prayer.entries.filter { it != Prayer.ASR }.forEach {
            assertEquals(standard.time(it), hanafi.time(it))
        }
    }

    @Test
    fun `method changes the angles - egyptian fajr is earlier than muslim world league fajr`() = runTest {
        val egyptian = calculator.calculate(cairo, date, settings(CalculationMethod.EGYPTIAN))
        val mwl = calculator.calculate(cairo, date, settings(CalculationMethod.MUSLIM_WORLD_LEAGUE))
        assertTrue(egyptian.time(Prayer.FAJR).isBefore(mwl.time(Prayer.FAJR)))
        assertEquals(egyptian.time(Prayer.DHUHR), mwl.time(Prayer.DHUHR))
    }

    @Test
    fun `every exposed method produces valid ordered times`() = runTest {
        CalculationMethod.entries.forEach { method ->
            val t = calculator.calculate(cairo, date, settings(method))
            val order = PrayerEvent.entries.map { t.time(it) }
            assertEquals("$method", order.sorted(), order)
        }
    }

    @Test
    fun `adjustment is applied exactly once and only to its prayer`() = runTest {
        val base = calculator.calculate(cairo, date, settings())
        val adjusted = calculator.calculate(
            cairo,
            date,
            settings(adjustments = PrayerAdjustments().with(Prayer.DHUHR, 2).with(Prayer.MAGHRIB, -3)),
        )
        assertEquals(2, Duration.between(base.time(Prayer.DHUHR), adjusted.time(Prayer.DHUHR)).toMinutes())
        assertEquals(-3, Duration.between(base.time(Prayer.MAGHRIB), adjusted.time(Prayer.MAGHRIB)).toMinutes())
        assertEquals(base.time(Prayer.FAJR), adjusted.time(Prayer.FAJR))
        assertEquals(base.time(PrayerEvent.SUNRISE), adjusted.time(PrayerEvent.SUNRISE))
    }

    @Test
    fun `local time follows the location time zone including daylight saving`() = runTest {
        val before = calculator.calculate(london, LocalDate.of(2026, 3, 28), settings(CalculationMethod.MUSLIM_WORLD_LEAGUE))
        val after = calculator.calculate(london, LocalDate.of(2026, 3, 29), settings(CalculationMethod.MUSLIM_WORLD_LEAGUE))
        fun localMinutes(t: com.hayati.app.domain.prayer.model.PrayerTimes): Int {
            val local = t.time(Prayer.DHUHR).atZone(t.zone).toLocalTime()
            return local.hour * 60 + local.minute
        }
        // Clocks go forward one hour on 2026-03-29 in London; solar noon moves by well under 2 minutes.
        val shift = localMinutes(after) - localMinutes(before)
        assertTrue("shift $shift", shift in 58..62)
    }

    @Test
    fun `the same inputs always give the same times`() = runTest {
        assertEquals(calculator.calculate(cairo, date, settings()), calculator.calculate(cairo, date, settings()))
    }

    @Test
    fun `invalid coordinates are rejected`() = runTest {
        val bad = cairo.copy(latitude = 123.0)
        try {
            calculator.calculate(bad, date, settings())
            fail("expected PrayerCalculationException")
        } catch (expected: PrayerCalculationException) {
            // ok
        }
    }
}
