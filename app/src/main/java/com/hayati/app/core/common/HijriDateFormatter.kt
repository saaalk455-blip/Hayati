package com.hayati.app.core.common

import java.time.DateTimeException
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.temporal.ChronoField

/**
 * Hijri date from java.time's built-in Umm al-Qura calendar (HijrahChronology), not invented.
 * That calendar is the official Saudi one and can differ by a day from a local moon sighting, so
 * the user can shift it by up to two days in settings. Outside the supported range it returns null
 * and the UI simply hides the date.
 */
object HijriDateFormatter {
    private val months = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
    )

    fun format(date: LocalDate, offsetDays: Int = 0): String? = try {
        val hijri = HijrahChronology.INSTANCE.date(date.plusDays(offsetDays.toLong()))
        val day = hijri.get(ChronoField.DAY_OF_MONTH)
        val month = hijri.get(ChronoField.MONTH_OF_YEAR)
        val year = hijri.get(ChronoField.YEAR)
        "$day ${months[month - 1]} $year هـ"
    } catch (failure: DateTimeException) {
        null
    }
}
