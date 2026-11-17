package com.hayati.app.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Re-schedules alerts whenever a setting that affects them changes (location, method, Asr,
 * adjustments, notification settings, time format) and once at app start. Also keeps the
 * maintenance job enrolled only while notifications are enabled, to avoid pointless wake-ups.
 */
class PrayerAutomation(
    private val context: Context,
    private val settingsRepository: PrayerSettingsRepository,
    private val coordinator: PrayerNotificationCoordinator,
    private val scope: CoroutineScope,
) {
    fun start() {
        scope.launch {
            settingsRepository.settings
                .map { it.affectingAlerts() }
                .distinctUntilChanged()
                .collect { relevant ->
                    try {
                        coordinator.reschedule()
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Exception) {
                        // Best effort: a scheduling failure must never crash the app process.
                    }
                    runCatching { updateMaintenance(enabled = relevant.notifications.enabled) }
                }
        }
    }

    private data class Relevant(
        val location: Any?,
        val calculation: Any,
        val notifications: com.hayati.app.domain.prayer.model.NotificationSettings,
        val use24Hour: Boolean,
    )

    private fun PrayerSettings.affectingAlerts() = Relevant(location, calculation, notifications, use24Hour)

    private fun updateMaintenance(enabled: Boolean) {
        val work = WorkManager.getInstance(context)
        if (enabled) {
            work.enqueueUniquePeriodicWork(
                PrayerMaintenanceWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<PrayerMaintenanceWorker>(12, TimeUnit.HOURS).build(),
            )
        } else {
            work.cancelUniqueWork(PrayerMaintenanceWorker.UNIQUE_NAME)
        }
    }
}
