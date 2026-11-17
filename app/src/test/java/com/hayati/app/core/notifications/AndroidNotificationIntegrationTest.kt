package com.hayati.app.core.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.hayati.app.domain.prayer.model.AlertKind
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.PlannedAlert
import com.hayati.app.domain.prayer.model.Prayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

/**
 * Robolectric tests of the Android glue (AlarmManager, receivers, notifications). They run on the
 * JVM with simulated system services: they show the wiring is correct, NOT that a real phone
 * delivers a notification. Real delivery must be tested on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidNotificationIntegrationTest {
    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager

    private val future = System.currentTimeMillis() + 3_600_000L

    private fun alert(code: Int, prayer: Prayer = Prayer.ASR, kind: AlertKind = AlertKind.AT_TIME, at: Long = future) =
        PlannedAlert(code, at, prayer, kind, 0, at, NotificationSound.DEFAULT, "Africa/Cairo", false)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        shadowOf(alarmManager).scheduledAlarms.clear()
    }

    @Test
    fun `scheduler registers an exact alarm and replacing the same id does not duplicate`() {
        val scheduler = AlarmManagerPrayerScheduler(context)
        scheduler.schedule(alert(1))
        scheduler.schedule(alert(1, at = future + 60_000))
        scheduler.schedule(alert(2))
        assertEquals(2, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun `cancel removes only that alarm`() {
        val scheduler = AlarmManagerPrayerScheduler(context)
        scheduler.schedule(alert(1))
        scheduler.schedule(alert(2))
        scheduler.cancel(1)
        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)
        scheduler.cancel(999) // unknown id is harmless
        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun `when the exact alarm permission is revoked the scheduler still schedules and reports it`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val scheduler = AlarmManagerPrayerScheduler(context)
        assertEquals(false, scheduler.canScheduleExact())
        scheduler.schedule(alert(1))
        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun `receiver posts a notification with the Arabic prayer wording`() {
        shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val now = System.currentTimeMillis()
        val intent = Intent(PrayerAlarmReceiver.ACTION_PRAYER_ALERT)
            .putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, Prayer.MAGHRIB.name)
            .putExtra(PrayerAlarmReceiver.EXTRA_KIND, AlertKind.AT_TIME.name)
            .putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, now)
            .putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, now)
            .putExtra(PrayerAlarmReceiver.EXTRA_ZONE, "Africa/Cairo")
        PrayerAlarmReceiver().onReceive(context, intent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val posted = shadowOf(manager).allNotifications
        assertEquals(1, posted.size)
        assertEquals(
            "حان الآن وقت صلاة المغرب",
            posted.single().extras.getCharSequence(android.app.Notification.EXTRA_TITLE).toString(),
        )
    }

    @Test
    fun `receiver ignores an unknown action and a malformed alert`() {
        shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        PrayerAlarmReceiver().onReceive(context, Intent("other.action"))
        PrayerAlarmReceiver().onReceive(context, Intent(PrayerAlarmReceiver.ACTION_PRAYER_ALERT))
        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }

    @Test
    fun `a stale reminder after the prayer time is not shown`() {
        shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val now = System.currentTimeMillis()
        val intent = Intent(PrayerAlarmReceiver.ACTION_PRAYER_ALERT)
            .putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, Prayer.ASR.name)
            .putExtra(PrayerAlarmReceiver.EXTRA_KIND, AlertKind.REMINDER.name)
            .putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, now - 20 * 60_000L)
            .putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, now - 10 * 60_000L)
        PrayerAlarmReceiver().onReceive(context, intent)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }

    @Test
    fun `boot, time zone, time change and update events are all handled by the system receiver`() {
        val pm = context.packageManager
        listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        ).forEach { action ->
            val receivers = pm.queryBroadcastReceivers(Intent(action).setPackage(context.packageName), PackageManager.MATCH_ALL)
            assertTrue("$action has no receiver", receivers.any { it.activityInfo.name == PrayerSystemEventReceiver::class.java.name })
        }
        assertNotNull(PrayerSystemEventReceiver.HANDLED_ACTIONS)
    }

    @Test
    fun `channels exist for sound and silent alerts`() {
        PrayerNotificationPresenter(context).ensureChannels()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertNotNull(manager.getNotificationChannel(PrayerNotificationPresenter.CHANNEL_SOUND))
        assertNotNull(manager.getNotificationChannel(PrayerNotificationPresenter.CHANNEL_SILENT))
        assertEquals(null, manager.getNotificationChannel(PrayerNotificationPresenter.CHANNEL_SILENT).sound)
    }
}
