package com.hayati.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hayati.app.HayatiApplication

/**
 * Low-frequency safety net: if an alarm chain was broken (force-stop, aggressive battery
 * management), this re-plans the window. It is NOT used for delivery: delivery is AlarmManager.
 */
class PrayerMaintenanceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? HayatiApplication ?: return Result.failure()
        app.container.notificationCoordinator.reschedule()
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "prayer_maintenance"
    }
}
