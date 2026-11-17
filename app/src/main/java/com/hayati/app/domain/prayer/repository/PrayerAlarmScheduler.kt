package com.hayati.app.domain.prayer.repository

import com.hayati.app.domain.prayer.model.PlannedAlert

/** Abstraction over the Android alarm service so scheduling logic can be tested with a fake. */
interface PrayerAlarmScheduler {
    /** Whether the system currently allows exact alarms for this app. */
    fun canScheduleExact(): Boolean

    /** Schedules (or replaces) the alert with the same [PlannedAlert.requestCode]. */
    fun schedule(alert: PlannedAlert)

    fun cancel(requestCode: Int)
}

/** Whether the user (or system) currently allows this app to show notifications. */
interface NotificationPermissionChecker {
    fun areNotificationsAllowed(): Boolean
}
