package com.hayati.app.core.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hayati.app.HayatiApplication
import kotlinx.coroutines.launch

/**
 * Alarms are cleared by a reboot and can become wrong after a clock or time zone change, so these
 * system events trigger a full re-schedule. Only the listed actions are handled.
 */
class PrayerSystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val app = context.applicationContext as? HayatiApplication ?: return
        val pending = goAsync()
        app.container.applicationScope.launch {
            try {
                app.container.notificationCoordinator.reschedule()
            } finally {
                pending?.finish()
            }
        }
    }

    companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
