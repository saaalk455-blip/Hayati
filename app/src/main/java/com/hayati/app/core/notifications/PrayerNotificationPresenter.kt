package com.hayati.app.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hayati.app.MainActivity
import com.hayati.app.R
import com.hayati.app.core.common.TimeFormatter
import com.hayati.app.core.common.labelRes
import com.hayati.app.domain.prayer.model.AlertKind
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.repository.NotificationPermissionChecker
import java.time.Instant
import java.time.ZoneId

/**
 * Builds and shows prayer notifications.
 *
 * Android makes a channel's sound immutable after creation, so "default sound" and "silent" are
 * separate channels. Users who want a different sound can pick one in the system channel settings
 * (the settings screen links there). No Adhan audio is bundled in this phase.
 */
class PrayerNotificationPresenter(context: Context) : NotificationPermissionChecker {
    private val appContext = context.applicationContext
    private val manager = NotificationManagerCompat.from(appContext)

    override fun areNotificationsAllowed(): Boolean = manager.areNotificationsEnabled()

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val system = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_SOUND, appContext.getString(R.string.channel_prayer_sound), NotificationManager.IMPORTANCE_HIGH).apply {
                description = appContext.getString(R.string.channel_prayer_sound_desc)
            },
        )
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_SILENT, appContext.getString(R.string.channel_prayer_silent), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = appContext.getString(R.string.channel_prayer_silent_desc)
                setSound(null, null)
                enableVibration(false)
            },
        )
    }

    /** Shows the alert if it is still meaningful. Returns true when a notification was posted. */
    @SuppressLint("MissingPermission") // guarded by areNotificationsAllowed() and a manual check
    fun show(
        prayer: Prayer,
        kind: AlertKind,
        triggerAtMillis: Long,
        prayerTimeMillis: Long,
        sound: NotificationSound,
        zoneId: String,
        use24Hour: Boolean,
        nowMillis: Long,
    ): Boolean {
        val variant = NotificationContent.variant(kind, triggerAtMillis, prayerTimeMillis, nowMillis) ?: return false
        if (!areNotificationsAllowed()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        ensureChannels()

        val name = appContext.getString(prayer.labelRes())
        val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
        val timeText = TimeFormatter.clock(
            Instant.ofEpochMilli(prayerTimeMillis), zone, use24Hour,
            appContext.getString(R.string.time_am), appContext.getString(R.string.time_pm),
        )
        val (title, body) = when (variant) {
            NotificationVariant.NOW ->
                appContext.getString(R.string.notif_now_title, name) to appContext.getString(R.string.notif_now_body)
            NotificationVariant.REMINDER -> appContext.getString(
                R.string.notif_reminder_title,
                NotificationContent.remainingMinutes(prayerTimeMillis, nowMillis),
                name,
            ) to appContext.getString(R.string.notif_reminder_body, timeText)
            NotificationVariant.LATE ->
                appContext.getString(R.string.notif_late_title, name, timeText) to appContext.getString(R.string.notif_late_body)
        }

        val open = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(
            appContext,
            if (sound == NotificationSound.SILENT) CHANNEL_SILENT else CHANNEL_SOUND,
        )
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        manager.notify(prayer.ordinal * 10 + kind.code, notification)
        return true
    }

    companion object {
        const val CHANNEL_SOUND = "prayer_sound"
        const val CHANNEL_SILENT = "prayer_silent"
    }
}
