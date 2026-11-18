package com.hayati.app.domain.prayer

import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.DailySummary
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerCalculationException
import com.hayati.app.domain.prayer.model.PrayerErrorReason
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerLog
import com.hayati.app.domain.prayer.model.PrayerRowState
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import com.hayati.app.testing.FakePrayerLogRepository
import com.hayati.app.testing.FakePrayerSettingsRepository
import com.hayati.app.testing.FakePrayerTimeCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class PrayerSnapshotProviderTest {
    private val location = PrayerLocation("مدينة", "بلد", 10.0, 10.0, "UTC", LocationSource.MANUAL)
    private val configured = PrayerSettings.default(CalculationMethod.EGYPTIAN).copy(location = location)
    private val day = LocalDate.of(2026, 6, 15)

    private fun at(hour: Int, minute: Int = 0, date: LocalDate = day) =
        date.atTime(hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun provider(
        settings: PrayerSettings = configured,
        logs: FakePrayerLogRepository = FakePrayerLogRepository(),
        calculator: FakePrayerTimeCalculator = FakePrayerTimeCalculator(),
    ) = PrayerSnapshotProvider(FakePrayerSettingsRepository(settings), logs, calculator)

    private suspend fun ready(p: PrayerSnapshotProvider, now: Long): PrayerSnapshot.Ready =
        p.observe(flowOf(now)).first() as PrayerSnapshot.Ready

    @Test
    fun `without a location the snapshot is NotConfigured`() = runTest {
        val p = provider(settings = PrayerSettings.default(CalculationMethod.EGYPTIAN))
        assertEquals(PrayerSnapshot.NotConfigured, p.observe(flowOf(at(8))).first())
    }

    @Test
    fun `calculation failure is reported, never hidden`() = runTest {
        val calc = FakePrayerTimeCalculator().apply { failure = PrayerCalculationException("x") }
        val snapshot = provider(calculator = calc).observe(flowOf(at(8))).first()
        assertEquals(PrayerSnapshot.Error(PrayerErrorReason.CALCULATION_FAILED), snapshot)
    }

    @Test
    fun `an invalid time zone is an error, not a crash`() = runTest {
        val bad = configured.copy(location = location.copy(zoneId = "Not/AZone"))
        assertEquals(
            PrayerSnapshot.Error(PrayerErrorReason.INVALID_LOCATION),
            provider(settings = bad).observe(flowOf(at(8))).first(),
        )
    }

    @Test
    fun `next prayer is the first prayer after now and sunrise is never next`() = runTest {
        // 04:00 is after Fajr (03:00) and after... sunrise is 05:00 and must be skipped.
        val s = ready(provider(), at(4))
        assertEquals(Prayer.DHUHR, s.next?.prayer)
        assertEquals(false, s.next?.isTomorrow)
        assertEquals(6, s.rows.size)
        assertNull(s.rows.first { it.event == PrayerEvent.SUNRISE }.completed)
        assertNull(s.rows.first { it.event == PrayerEvent.SUNRISE }.notificationActive)
    }

    @Test
    fun `before fajr the next prayer is today's fajr`() = runTest {
        val s = ready(provider(), at(1))
        assertEquals(Prayer.FAJR, s.next?.prayer)
        assertEquals(false, s.next?.isTomorrow)
    }

    @Test
    fun `after isha the next prayer is tomorrow's fajr`() = runTest {
        val s = ready(provider(), at(21))
        assertEquals(Prayer.FAJR, s.next?.prayer)
        assertEquals(true, s.next?.isTomorrow)
        assertEquals(at(3, date = day.plusDays(1)), s.next?.time?.toEpochMilli())
        assertTrue(s.rows.none { it.state == PrayerRowState.NEXT })
    }

    @Test
    fun `remaining time is real and never negative`() = runTest {
        val s = ready(provider(), at(9, 30)) // dhuhr at 10:00
        assertEquals(30 * 60 * 1000L, s.remainingMillis)
    }

    @Test
    fun `a passed prayer without a record is unrecorded, not missed`() = runTest {
        val s = ready(provider(), at(14))
        val fajr = s.rows.first { it.event == PrayerEvent.FAJR }
        assertEquals(PrayerRowState.UNRECORDED, fajr.state)
        assertEquals(false, fajr.completed)
        assertEquals(PrayerRowState.PASSED, s.rows.first { it.event == PrayerEvent.SUNRISE }.state)
        assertEquals(PrayerRowState.UNRECORDED, s.rows.first { it.event == PrayerEvent.ASR }.state)
        assertEquals(PrayerRowState.NEXT, s.rows.first { it.event == PrayerEvent.MAGHRIB }.state)
        assertEquals(PrayerRowState.UPCOMING, s.rows.first { it.event == PrayerEvent.ISHA }.state)
    }

    @Test
    fun `only prayers the user recorded are completed and counted`() = runTest {
        val logs = FakePrayerLogRepository()
        logs.setCompleted(day.toEpochDay(), Prayer.FAJR, true)
        logs.setCompleted(day.toEpochDay(), Prayer.DHUHR, true)
        val s = ready(provider(logs = logs), at(14))
        assertEquals(PrayerRowState.COMPLETED, s.rows.first { it.event == PrayerEvent.FAJR }.state)
        assertEquals(PrayerRowState.UNRECORDED, s.rows.first { it.event == PrayerEvent.ASR }.state)
        assertEquals(DailySummary(completed = 2, remaining = 3, percent = 40), s.summary)
    }

    @Test
    fun `the prayer day changes at local midnight of the location zone`() = runTest {
        val tokyo = configured.copy(location = location.copy(zoneId = "Asia/Tokyo"))
        val p = provider(settings = tokyo)
        // 2026-06-15 14:30 UTC is 23:30 in Tokyo (same date); 15:30 UTC is 00:30 on the 16th.
        assertEquals(LocalDate.of(2026, 6, 15), ready(p, at(14, 30)).date)
        assertEquals(LocalDate.of(2026, 6, 16), ready(p, at(15, 30)).date)
    }

    @Test
    fun `daily summary counts only the five prayers and computes the percentage`() {
        fun log(p: Prayer, done: Boolean) = PrayerLog(1, 1, p, done, null)
        assertEquals(DailySummary(0, 5, 0), DailySummary.from(emptyList()))
        assertEquals(DailySummary(5, 0, 100), DailySummary.from(Prayer.entries.map { log(it, true) }))
        assertEquals(DailySummary(3, 2, 60), DailySummary.from(Prayer.entries.take(3).map { log(it, true) } + log(Prayer.ISHA, false)))
    }
}
