package com.hayati.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hayati.app.HayatiApplication
import com.hayati.app.domain.prayer.model.NotificationSound
import kotlinx.coroutines.launch

/** Receives a scheduled prayer alert, shows it, and rolls the scheduling window forward. */
class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PRAYER_ALERT) return
        val app = context.applicationContext as? HayatiApplication ?: return
        val prayer = intent.getStringExtra(EXTRA_PRAYER).toPrayerOrNull() ?: return
        val kind = intent.getStringExtra(EXTRA_KIND).toKindOrNull() ?: return
        val container = app.container

        container.notificationPresenter.show(
            prayer = prayer,
            kind = kind,
            triggerAtMillis = intent.getLongExtra(EXTRA_TRIGGER_AT, 0L),
            prayerTimeMillis = intent.getLongExtra(EXTRA_PRAYER_TIME, 0L),
            sound = NotificationSound.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_SOUND) }
                ?: NotificationSound.DEFAULT,
            zoneId = intent.getStringExtra(EXTRA_ZONE).orEmpty(),
            use24Hour = intent.getBooleanExtra(EXTRA_USE_24H, false),
            nowMillis = container.timeProvider.nowMillis(),
        )

        // Schedule the next alerts. goAsync keeps the process alive until this finishes.
        val pending = goAsync()
        container.applicationScope.launch {
            try {
                container.notificationCoordinator.reschedule()
            } finally {
                pending?.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_ALERT = "com.hayati.app.action.PRAYER_ALERT"
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_KIND = "kind"
        const val EXTRA_TRIGGER_AT = "trigger_at"
        const val EXTRA_PRAYER_TIME = "prayer_time"
        const val EXTRA_SOUND = "sound"
        const val EXTRA_ZONE = "zone"
        const val EXTRA_USE_24H = "use_24h"
    }
}
