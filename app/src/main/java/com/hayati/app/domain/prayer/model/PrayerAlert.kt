package com.hayati.app.domain.prayer.model

enum class AlertKind(val code: Int) { AT_TIME(0), REMINDER(1) }

/** One notification that should be delivered by the system alarm service. */
data class PlannedAlert(
    /** Stable per (local date, prayer, kind): the same alert always has the same id. */
    val requestCode: Int,
    val triggerAtMillis: Long,
    val prayer: Prayer,
    val kind: AlertKind,
    val reminderMinutes: Int,
    val prayerTimeMillis: Long,
    val sound: NotificationSound,
    val zoneId: String,
    val use24Hour: Boolean,
)
