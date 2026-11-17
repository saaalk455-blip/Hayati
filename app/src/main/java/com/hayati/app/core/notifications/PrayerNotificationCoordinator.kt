package com.hayati.app.core.notifications

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.domain.prayer.model.PrayerCalculationException
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.repository.NotificationPermissionChecker
import com.hayati.app.domain.prayer.repository.PrayerAlarmScheduler
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import com.hayati.app.domain.prayer.repository.PrayerTimeCalculator
import com.hayati.app.domain.prayer.usecase.AlertPlanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

enum class RescheduleOutcome {
    /** Alarms were (re)scheduled. */
    SCHEDULED,

    /** Notifications are off, or no location: everything was cancelled, nothing scheduled. */
    NOTHING_TO_SCHEDULE,

    /** The user wants notifications but the system blocks them: alarms were cancelled. */
    PERMISSION_DENIED,

    /** Times could not be calculated; previously planned alarms were left untouched. */
    CALCULATION_FAILED,
}

data class RescheduleResult(
    val outcome: RescheduleOutcome,
    val scheduledCount: Int = 0,
    /** False means alerts are inexact: the system may deliver them a few minutes late. */
    val exact: Boolean = true,
)

/**
 * Keeps the system alarms in line with settings and calculated times.
 *
 * Window: today and tomorrow (in the prayer location's zone). Each alarm firing, app start,
 * boot, time/zone change and a periodic maintenance job call [reschedule] again, so the window
 * rolls forward without scheduling days in advance. Alarm ids are stable, so the same alert is
 * replaced rather than duplicated, and stale ids in a wider window are cancelled first.
 */
class PrayerNotificationCoordinator(
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerTimeCalculator,
    private val scheduler: PrayerAlarmScheduler,
    private val permission: NotificationPermissionChecker,
    private val time: TimeProvider,
) {
    private val mutex = Mutex()

    suspend fun reschedule(): RescheduleResult = mutex.withLock {
        val settings = settingsRepository.settings.first()
        val nowMillis = time.nowMillis()
        val location = settings.location
        val zone = location?.takeIf { PrayerLocation.isValidZone(it.zoneId) }?.zone
        val today = zone?.let { Instant.ofEpochMilli(nowMillis).atZone(it).toLocalDate() }

        if (location == null || zone == null || today == null || !settings.notifications.enabled) {
            cancelWindow(today ?: Instant.ofEpochMilli(nowMillis).atZone(java.time.ZoneOffset.UTC).toLocalDate())
            return RescheduleResult(RescheduleOutcome.NOTHING_TO_SCHEDULE)
        }
        if (!permission.areNotificationsAllowed()) {
            cancelWindow(today)
            return RescheduleResult(RescheduleOutcome.PERMISSION_DENIED)
        }

        val days = try {
            listOf(today, today.plusDays(1)).map { calculator.calculate(location, it, settings.calculation) }
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: PrayerCalculationException) {
            return RescheduleResult(RescheduleOutcome.CALCULATION_FAILED)
        }

        cancelWindow(today)
        val alerts = AlertPlanner.plan(nowMillis, days, settings.notifications, settings.use24Hour)
        alerts.forEach(scheduler::schedule)
        RescheduleResult(RescheduleOutcome.SCHEDULED, alerts.size, exact = scheduler.canScheduleExact())
    }

    /** Cancels every possible alert id from yesterday to two days ahead. */
    private fun cancelWindow(today: java.time.LocalDate) {
        (-1L..2L).forEach { offset ->
            AlertPlanner.allCodesFor(today.plusDays(offset)).forEach(scheduler::cancel)
        }
    }
}
