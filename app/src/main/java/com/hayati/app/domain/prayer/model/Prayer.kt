package com.hayati.app.domain.prayer.model

/** The five obligatory prayers. Sunrise is deliberately not part of this enum. */
enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }

/** Every event shown in the daily schedule: the five prayers plus sunrise. */
enum class PrayerEvent(val prayer: Prayer?) {
    FAJR(Prayer.FAJR),
    SUNRISE(null),
    DHUHR(Prayer.DHUHR),
    ASR(Prayer.ASR),
    MAGHRIB(Prayer.MAGHRIB),
    ISHA(Prayer.ISHA),
    ;

    companion object {
        fun of(prayer: Prayer): PrayerEvent = when (prayer) {
            Prayer.FAJR -> FAJR
            Prayer.DHUHR -> DHUHR
            Prayer.ASR -> ASR
            Prayer.MAGHRIB -> MAGHRIB
            Prayer.ISHA -> ISHA
        }
    }
}
