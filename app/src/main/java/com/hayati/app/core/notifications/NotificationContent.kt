package com.hayati.app.core.notifications

import com.hayati.app.domain.prayer.model.AlertKind

/** What a delivered alert should say. Pure, so wording rules are tested without Android. */
enum class NotificationVariant {
    /** Delivered on time: "it is now time for X". */
    NOW,

    /** Pre-prayer reminder: "N minutes left" (N computed from the real clock). */
    REMINDER,

    /**
     * An at-time alert delivered noticeably late (device asleep, inexact alarm, restart).
     * It must not claim "now"; it states the real prayer time instead.
     */
    LATE,
}

object NotificationContent {
    const val LATE_THRESHOLD_MILLIS = 10 * 60_000L

    /** Returns null when the alert is no longer meaningful and must not be shown. */
    fun variant(
        kind: AlertKind,
        triggerAtMillis: Long,
        prayerTimeMillis: Long,
        nowMillis: Long,
    ): NotificationVariant? = when {
        // A reminder that arrives after the prayer time would be misleading: drop it.
        kind == AlertKind.REMINDER -> if (nowMillis < prayerTimeMillis) NotificationVariant.REMINDER else null
        nowMillis - triggerAtMillis > LATE_THRESHOLD_MILLIS -> NotificationVariant.LATE
        else -> NotificationVariant.NOW
    }

    /** Whole minutes left, rounded up, never below one. */
    fun remainingMinutes(prayerTimeMillis: Long, nowMillis: Long): Int =
        (((prayerTimeMillis - nowMillis) + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
}
