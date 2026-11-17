package com.hayati.app.domain.prayer.model

/** Calculation methods Hayati exposes. Each one is implemented by the chosen engine (adhan2). */
enum class CalculationMethod {
    MUSLIM_WORLD_LEAGUE,
    EGYPTIAN,
    UMM_AL_QURA,
    NORTH_AMERICA,
    KARACHI,
    DUBAI,
    QATAR,
    KUWAIT,
    TURKEY,
    SINGAPORE,
}

/** Standard = Shafi'i/Maliki/Hanbali (shadow length 1). Hanafi = shadow length 2 (later Asr). */
enum class AsrMethod { STANDARD, HANAFI }

/** Manual minute adjustment per prayer, applied once, inside the calculator. */
data class PrayerAdjustments(val minutes: Map<Prayer, Int> = emptyMap()) {
    fun of(prayer: Prayer): Int = (minutes[prayer] ?: 0).coerceIn(MIN, MAX)

    fun with(prayer: Prayer, value: Int): PrayerAdjustments =
        copy(minutes = minutes + (prayer to value.coerceIn(MIN, MAX)))

    companion object {
        const val MIN = -30
        const val MAX = 30
    }
}

data class PrayerCalculationSettings(
    val method: CalculationMethod,
    val asr: AsrMethod,
    val adjustments: PrayerAdjustments,
)

enum class NotificationSound { DEFAULT, SILENT }

data class PrayerNotificationConfig(
    val enabled: Boolean = false,
    /** Extra reminder before the prayer; 0 = none. Allowed values: [REMINDER_OPTIONS]. */
    val reminderMinutes: Int = 0,
    val sound: NotificationSound = NotificationSound.DEFAULT,
) {
    companion object {
        val REMINDER_OPTIONS = listOf(0, 5, 10, 15)
    }
}

data class NotificationSettings(
    /** Master switch. Off by default: notifications are never enabled without the user's choice. */
    val enabled: Boolean = false,
    val perPrayer: Map<Prayer, PrayerNotificationConfig> = emptyMap(),
) {
    fun config(prayer: Prayer): PrayerNotificationConfig = perPrayer[prayer] ?: PrayerNotificationConfig()

    fun isActive(prayer: Prayer): Boolean = enabled && config(prayer).enabled
}

data class PrayerSettings(
    val location: PrayerLocation?,
    val calculation: PrayerCalculationSettings,
    /** True once the user picked a method themselves, so a recommended default never overrides it. */
    val methodChosenByUser: Boolean,
    val notifications: NotificationSettings,
    val use24Hour: Boolean,
    /** Manual correction (days) for the Hijri date, because calendars can differ by a day. */
    val hijriOffsetDays: Int,
) {
    companion object {
        const val HIJRI_OFFSET_MIN = -2
        const val HIJRI_OFFSET_MAX = 2

        fun default(method: CalculationMethod): PrayerSettings = PrayerSettings(
            location = null,
            calculation = PrayerCalculationSettings(method, AsrMethod.STANDARD, PrayerAdjustments()),
            methodChosenByUser = false,
            notifications = NotificationSettings(),
            use24Hour = false,
            hijriOffsetDays = 0,
        )
    }
}
