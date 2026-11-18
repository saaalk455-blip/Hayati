package com.hayati.app.domain.prayer

import com.hayati.app.core.common.HijriDateFormatter
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLog
import com.hayati.app.domain.prayer.usecase.HistoryBuilder
import com.hayati.app.domain.prayer.usecase.ManualLocationError
import com.hayati.app.domain.prayer.usecase.ManualLocationResult
import com.hayati.app.domain.prayer.usecase.ManualLocationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrayerPureLogicTest {
    private fun log(day: Long, prayer: Prayer, done: Boolean) = PrayerLog(1, day, prayer, done, null)

    @Test
    fun `history lists only days with recorded prayers, newest first, and invents nothing`() {
        val days = HistoryBuilder.build(
            listOf(
                log(10, Prayer.FAJR, true),
                log(10, Prayer.DHUHR, true),
                log(12, Prayer.ASR, true),
                log(11, Prayer.ISHA, false), // reverted: not shown
            ),
        )
        assertEquals(listOf(12L, 10L), days.map { it.epochDay })
        assertEquals(setOf(Prayer.FAJR, Prayer.DHUHR), days.last().completed)
        assertEquals(2, days.last().count)
        assertTrue(HistoryBuilder.build(emptyList()).isEmpty())
    }

    @Test
    fun `manual location accepts valid input including Arabic digits and decimal commas`() {
        val result = ManualLocationValidator.validate("القاهرة", "مصر", "٣٠٫٠٤٤٤", "31,2357", " Africa/Cairo ")
        val location = (result as ManualLocationResult.Valid).location
        assertEquals(30.0444, location.latitude, 1e-9)
        assertEquals(31.2357, location.longitude, 1e-9)
        assertEquals("Africa/Cairo", location.zoneId)
    }

    @Test
    fun `manual location reports every invalid field`() {
        val result = ManualLocationValidator.validate("", "", "95", "abc", "Mars/Base")
        assertEquals(
            setOf(ManualLocationError.LATITUDE, ManualLocationError.LONGITUDE, ManualLocationError.ZONE),
            (result as ManualLocationResult.Invalid).errors,
        )
        assertTrue(ManualLocationValidator.validate("", "", "-90", "180", "UTC") is ManualLocationResult.Valid)
        assertTrue(ManualLocationValidator.validate("", "", "90.0001", "0", "UTC") is ManualLocationResult.Invalid)
        assertNull(ManualLocationValidator.parse("NaN"))
    }

    @Test
    fun `hijri date comes from the Umm al-Qura calendar and honours the offset`() {
        // 1 Muharram 1447 AH began on 26 June 2025 in the Umm al-Qura calendar.
        assertEquals("1 محرم 1447 هـ", HijriDateFormatter.format(LocalDate.of(2025, 6, 26)))
        assertEquals("2 محرم 1447 هـ", HijriDateFormatter.format(LocalDate.of(2025, 6, 26), offsetDays = 1))
        assertNotNull(HijriDateFormatter.format(LocalDate.of(2026, 11, 18)))
    }

    @Test
    fun `hijri date is hidden outside the supported range instead of being invented`() {
        assertNull(HijriDateFormatter.format(LocalDate.of(1700, 1, 1)))
    }
}
