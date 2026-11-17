package com.hayati.app.core.common

import java.time.LocalDate

/**
 * Source of "now" and "today". Injected so day-dependent logic can be tested without depending on
 * the real clock.
 */
interface TimeProvider {
    /** Today's date in the device time zone, as days since 1970-01-01. */
    fun todayEpochDay(): Long

    fun nowMillis(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    override fun nowMillis(): Long = System.currentTimeMillis()
}
