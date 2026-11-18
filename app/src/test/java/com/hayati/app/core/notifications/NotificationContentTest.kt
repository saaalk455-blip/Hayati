package com.hayati.app.core.notifications

import com.hayati.app.domain.prayer.model.AlertKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationContentTest {
    private val prayer = 1_000_000_000L
    private val minute = 60_000L

    @Test
    fun `on-time alert says it is now time`() {
        assertEquals(NotificationVariant.NOW, NotificationContent.variant(AlertKind.AT_TIME, prayer, prayer, prayer + 20_000))
    }

    @Test
    fun `an at-time alert delivered more than ten minutes late must not claim now`() {
        assertEquals(NotificationVariant.NOW, NotificationContent.variant(AlertKind.AT_TIME, prayer, prayer, prayer + 10 * minute))
        assertEquals(NotificationVariant.LATE, NotificationContent.variant(AlertKind.AT_TIME, prayer, prayer, prayer + 11 * minute))
    }

    @Test
    fun `a reminder is shown only before the prayer time`() {
        val trigger = prayer - 10 * minute
        assertEquals(NotificationVariant.REMINDER, NotificationContent.variant(AlertKind.REMINDER, trigger, prayer, trigger + 5_000))
        assertNull(NotificationContent.variant(AlertKind.REMINDER, trigger, prayer, prayer))
        assertNull(NotificationContent.variant(AlertKind.REMINDER, trigger, prayer, prayer + minute))
    }

    @Test
    fun `remaining minutes come from the real clock, rounded up, minimum one`() {
        assertEquals(10, NotificationContent.remainingMinutes(prayer, prayer - 10 * minute))
        assertEquals(10, NotificationContent.remainingMinutes(prayer, prayer - 9 * minute - 30_000))
        assertEquals(1, NotificationContent.remainingMinutes(prayer, prayer - 1_000))
        assertEquals(1, NotificationContent.remainingMinutes(prayer, prayer + 5_000))
    }
}
