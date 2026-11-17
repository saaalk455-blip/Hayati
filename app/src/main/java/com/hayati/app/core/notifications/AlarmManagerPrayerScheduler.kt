package com.hayati.app.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.hayati.app.domain.prayer.model.AlertKind
import com.hayati.app.domain.prayer.model.PlannedAlert
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.repository.PrayerAlarmScheduler

/**
 * Schedules prayer alerts with [AlarmManager].
 *
 * Exact scheduling (setExactAndAllowWhileIdle) is used only when the system allows it. On Android
 * 12+ the user can revoke the "Alarms & reminders" permission; then we fall back to
 * setAndAllowWhileIdle, which the system may delay by several minutes, and the UI says so.
 */
class AlarmManagerPrayerScheduler(context: Context) : PrayerAlarmScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    override fun schedule(alert: PlannedAlert) {
        val pending = pendingIntent(alert.requestCode, alert, create = true) ?: return
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.triggerAtMillis, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.triggerAtMillis, pending)
            }
        } catch (failure: SecurityException) {
            // Permission revoked between the check and the call: degrade to an inexact alarm.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.triggerAtMillis, pending)
        }
    }

    override fun cancel(requestCode: Int) {
        val pending = pendingIntent(requestCode, alert = null, create = false) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(requestCode: Int, alert: PlannedAlert?, create: Boolean): PendingIntent? {
        val intent = Intent(appContext, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRAYER_ALERT
            // The data URI makes each alert's intent unique, so alerts never overwrite each other.
            data = Uri.parse("hayati://prayer-alert/$requestCode")
            if (alert != null) {
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, alert.prayer.name)
                putExtra(PrayerAlarmReceiver.EXTRA_KIND, alert.kind.name)
                putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, alert.triggerAtMillis)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, alert.prayerTimeMillis)
                putExtra(PrayerAlarmReceiver.EXTRA_SOUND, alert.sound.name)
                putExtra(PrayerAlarmReceiver.EXTRA_ZONE, alert.zoneId)
                putExtra(PrayerAlarmReceiver.EXTRA_USE_24H, alert.use24Hour)
            }
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(appContext, requestCode, intent, flags)
    }
}

internal fun String?.toPrayerOrNull(): Prayer? = Prayer.entries.firstOrNull { it.name == this }

internal fun String?.toKindOrNull(): AlertKind? = AlertKind.entries.firstOrNull { it.name == this }
