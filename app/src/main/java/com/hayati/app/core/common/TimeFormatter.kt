package com.hayati.app.core.common

import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** Pure formatting helpers (no Android types) so they can be tested directly. */
object TimeFormatter {
    fun clock(instant: Instant, zone: ZoneId, use24Hour: Boolean, am: String, pm: String): String {
        val t = instant.atZone(zone).toLocalTime()
        if (use24Hour) return String.format(Locale.ROOT, "%02d:%02d", t.hour, t.minute)
        val hour12 = (t.hour % 12).let { if (it == 0) 12 else it }
        return String.format(Locale.ROOT, "%d:%02d %s", hour12, t.minute, if (t.hour < 12) am else pm)
    }

    /** HH:mm:ss for a remaining duration; negative input is shown as zero. */
    fun countdown(millis: Long): String {
        val total = (millis.coerceAtLeast(0L)) / 1000
        return String.format(Locale.ROOT, "%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60)
    }
}
