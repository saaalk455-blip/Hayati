package com.hayati.app.core.notifications

import com.hayati.app.data.prayer.calculator.AdhanPrayerTimeCalculator
import com.hayati.app.domain.prayer.model.AlertKind
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NotificationSettings
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerNotificationConfig
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.usecase.AlertPlanner
import com.hayati.app.testing.FakeAlarmScheduler
import com.hayati.app.testing.FakeNotificationPermission
import com.hayati.app.testing.FakePrayerSettingsRepository
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/** Uses the real adhan2 engine so setting changes visibly move the scheduled alerts. */
class PrayerNotificationCoordinatorTest {
    private val cairo = PrayerLocation("القاهرة", "مصر", 30.0444, 31.2357, "Africa/Cairo", LocationSource.MANUAL)
    private val mecca = PrayerLocation("مكة", "السعودية", 21.3891, 39.8579, "Asia/Riyadh", LocationSource.MANUAL)

    // 2026-06-15 03:00 UTC = 06:00 in Cairo: after Fajr, before Dhuhr.
    private val now = Instant.parse("2026-06-15T03:00:00Z").toEpochMilli()

    private val allOn = NotificationSettings(
        enabled = true,
        perPrayer = Prayer.entries.associateWith { PrayerNotificationConfig(enabled = true) },
    )

    private val scheduler = FakeAlarmScheduler()
    private val permission = FakeNotificationPermission()
    private val time = FakeTimeProvider(now = now)

    private fun settings(
        location: PrayerLocation? = cairo,
        notifications: NotificationSettings = allOn,
    ) = PrayerSettings.default(CalculationMethod.EGYPTIAN).copy(location = location, notifications = notifications)

    private fun coordinator(repo: FakePrayerSettingsRepository) = PrayerNotificationCoordinator(
        repo, AdhanPrayerTimeCalculator(), scheduler, permission, time,
    )

    @Test
    fun `schedules only future alerts for enabled prayers over today and tomorrow`() = runTest {
        val result = coordinator(FakePrayerSettingsRepository(settings())).reschedule()
        assertEquals(RescheduleOutcome.SCHEDULED, result.outcome)
        // Today: Fajr already passed -> 4 left. Tomorrow: 5. Total 9 at-time alerts.
        assertEquals(9, result.scheduledCount)
        assertEquals(9, scheduler.alarms.size)
        assertTrue(scheduler.alarms.values.all { it.triggerAtMillis > now })
        assertTrue(scheduler.alarms.values.all { it.kind == AlertKind.AT_TIME })
    }

    @Test
    fun `only prayers the user enabled are scheduled and reminders are added`() = runTest {
        val partial = NotificationSettings(
            enabled = true,
            perPrayer = mapOf(
                Prayer.MAGHRIB to PrayerNotificationConfig(true, reminderMinutes = 10, sound = NotificationSound.SILENT),
            ),
        )
        coordinator(FakePrayerSettingsRepository(settings(notifications = partial))).reschedule()
        assertTrue(scheduler.alarms.values.all { it.prayer == Prayer.MAGHRIB })
        assertEquals(4, scheduler.alarms.size) // today + tomorrow, at-time + reminder
        val today = scheduler.alarms.values.filter { it.kind == AlertKind.AT_TIME }.minBy { it.triggerAtMillis }
        val reminder = scheduler.alarms.values.filter { it.kind == AlertKind.REMINDER }.minBy { it.triggerAtMillis }
        assertEquals(today.prayerTimeMillis - 10 * 60_000L, reminder.triggerAtMillis)
        assertTrue(scheduler.alarms.values.all { it.sound == NotificationSound.SILENT })
    }

    @Test
    fun `rescheduling twice never creates duplicates`() = runTest {
        val c = coordinator(FakePrayerSettingsRepository(settings()))
        c.reschedule()
        val first = scheduler.alarms.keys.toSet()
        c.reschedule()
        assertEquals(first, scheduler.alarms.keys)
        assertEquals(9, scheduler.alarms.size)
    }

    @Test
    fun `master switch off cancels everything`() = runTest {
        val repo = FakePrayerSettingsRepository(settings())
        val c = coordinator(repo)
        c.reschedule()
        assertTrue(scheduler.alarms.isNotEmpty())
        repo.update { it.copy(notifications = it.notifications.copy(enabled = false)) }
        val result = c.reschedule()
        assertEquals(RescheduleOutcome.NOTHING_TO_SCHEDULE, result.outcome)
        assertTrue(scheduler.alarms.isEmpty())
    }

    @Test
    fun `disabling one prayer cancels only its alerts`() = runTest {
        val repo = FakePrayerSettingsRepository(settings())
        val c = coordinator(repo)
        c.reschedule()
        repo.update { s ->
            s.copy(notifications = s.notifications.copy(
                perPrayer = s.notifications.perPrayer + (Prayer.ISHA to PrayerNotificationConfig(enabled = false)),
            ))
        }
        c.reschedule()
        assertTrue(scheduler.alarms.values.none { it.prayer == Prayer.ISHA })
        assertEquals(7, scheduler.alarms.size)
    }

    @Test
    fun `no location means nothing is scheduled`() = runTest {
        val result = coordinator(FakePrayerSettingsRepository(settings(location = null))).reschedule()
        assertEquals(RescheduleOutcome.NOTHING_TO_SCHEDULE, result.outcome)
        assertTrue(scheduler.alarms.isEmpty())
    }

    @Test
    fun `location change moves the alerts to the new times without leaving old ones`() = runTest {
        val repo = FakePrayerSettingsRepository(settings())
        val c = coordinator(repo)
        c.reschedule()
        val before = scheduler.alarms.values.associate { it.requestCode to it.triggerAtMillis }
        repo.update { it.copy(location = mecca) }
        c.reschedule()
        val after = scheduler.alarms.values.associate { it.requestCode to it.triggerAtMillis }
        assertEquals(9, after.size)
        assertNotEquals(before, after)
        assertTrue(scheduler.alarms.values.all { it.zoneId == "Asia/Riyadh" })
    }

    @Test
    fun `calculation setting changes move the affected alert`() = runTest {
        val repo = FakePrayerSettingsRepository(settings())
        val c = coordinator(repo)
        c.reschedule()
        fun asr() = scheduler.alarms.values.filter { it.prayer == Prayer.ASR }.minBy { it.triggerAtMillis }.triggerAtMillis
        fun fajr() = scheduler.alarms.values.filter { it.prayer == Prayer.FAJR }.minBy { it.triggerAtMillis }.triggerAtMillis
        val asrStandard = asr()
        val fajrBefore = fajr()
        repo.update { it.copy(calculation = it.calculation.copy(asr = AsrMethod.HANAFI)) }
        c.reschedule()
        assertTrue(asr() > asrStandard)
        assertEquals(fajrBefore, fajr())

        val dhuhrBefore = scheduler.alarms.values.filter { it.prayer == Prayer.DHUHR }.minBy { it.triggerAtMillis }.triggerAtMillis
        repo.update { it.copy(calculation = it.calculation.copy(adjustments = it.calculation.adjustments.with(Prayer.DHUHR, 5))) }
        c.reschedule()
        val dhuhrAfter = scheduler.alarms.values.filter { it.prayer == Prayer.DHUHR }.minBy { it.triggerAtMillis }.triggerAtMillis
        assertEquals(5 * 60_000L, dhuhrAfter - dhuhrBefore)
    }

    @Test
    fun `denied notification permission cancels alarms and reports it`() = runTest {
        val c = coordinator(FakePrayerSettingsRepository(settings()))
        c.reschedule()
        permission.allowed = false
        val result = c.reschedule()
        assertEquals(RescheduleOutcome.PERMISSION_DENIED, result.outcome)
        assertTrue(scheduler.alarms.isEmpty())
    }

    @Test
    fun `when exact alarms are unavailable alerts are still scheduled and flagged inexact`() = runTest {
        scheduler.exactAllowed = false
        val result = coordinator(FakePrayerSettingsRepository(settings())).reschedule()
        assertEquals(RescheduleOutcome.SCHEDULED, result.outcome)
        assertFalse(result.exact)
        assertEquals(9, scheduler.alarms.size)
    }

    @Test
    fun `alert ids follow the prayer location's date so a zone ahead of UTC uses its own date`() = runTest {
        // 2026-06-15 23:30 UTC is already 2026-06-16 in Cairo (UTC+3 in summer).
        time.now = Instant.parse("2026-06-15T23:30:00Z").toEpochMilli()
        coordinator(FakePrayerSettingsRepository(settings())).reschedule()
        val dates = setOf(LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 17))
        val expected = dates.flatMap { d -> Prayer.entries.map { AlertPlanner.requestCode(d, it, AlertKind.AT_TIME) } }.toSet()
        assertEquals(expected, scheduler.alarms.keys)
    }

    @Test
    fun `after a reboot cleared the alarms, a reschedule restores them`() = runTest {
        val c = coordinator(FakePrayerSettingsRepository(settings()))
        c.reschedule()
        val before = scheduler.alarms.toMap()
        scheduler.alarms.clear() // what a device restart does to AlarmManager
        c.reschedule()
        assertEquals(before, scheduler.alarms)
    }

    @Test
    fun `alert request codes are unique per date, prayer and kind`() {
        val codes = (0L..5L).flatMap { d ->
            AlertPlanner.allCodesFor(LocalDate.ofEpochDay(20_000 + d))
        }
        assertEquals(codes.size, codes.toSet().size)
    }
}
