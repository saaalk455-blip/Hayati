package com.hayati.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TimeFormatterTest {
    private val cairo = ZoneId.of("Africa/Cairo")
    private fun at(utc: String) = Instant.parse(utc)

    @Test
    fun `24 hour format`() {
        assertEquals("14:05", TimeFormatter.clock(at("2026-01-15T12:05:00Z"), cairo, true, "ص", "م"))
    }

    @Test
    fun `12 hour format handles midnight and noon`() {
        assertEquals("12:30 ص", TimeFormatter.clock(at("2026-01-14T22:30:00Z"), cairo, false, "ص", "م"))
        assertEquals("12:00 م", TimeFormatter.clock(at("2026-01-15T10:00:00Z"), cairo, false, "ص", "م"))
        assertEquals("1:05 م", TimeFormatter.clock(at("2026-01-15T11:05:00Z"), cairo, false, "ص", "م"))
    }

    @Test
    fun `time is shown in the given zone`() {
        assertEquals("12:00", TimeFormatter.clock(at("2026-01-15T12:00:00Z"), ZoneId.of("UTC"), true, "", ""))
        assertEquals("14:00", TimeFormatter.clock(at("2026-01-15T12:00:00Z"), cairo, true, "", ""))
    }

    @Test
    fun `countdown formatting`() {
        assertEquals("01:25:40", TimeFormatter.countdown(((1 * 60 + 25) * 60 + 40) * 1000L))
        assertEquals("00:00:00", TimeFormatter.countdown(-500))
        assertEquals("00:00:59", TimeFormatter.countdown(59_999))
        assertEquals("10:00:00", TimeFormatter.countdown(10 * 3600_000L))
    }
}
