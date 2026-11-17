package com.hayati.app.domain.prayer.model

/**
 * Small offline list of cities so manual location works with no internet and no geocoding
 * service. Coordinates are approximate city centres; the user can always enter exact ones.
 * [recommended] is only a suggestion for the calculation method; the user can change it.
 */
data class KnownCity(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val recommended: CalculationMethod,
)

object KnownCities {
    val all: List<KnownCity> = listOf(
        KnownCity("القاهرة", "مصر", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("الجيزة", "مصر", 30.0131, 31.2089, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("الإسكندرية", "مصر", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("المنصورة", "مصر", 31.0409, 31.3785, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("الأقصر", "مصر", 25.6872, 32.6396, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("أسوان", "مصر", 24.0889, 32.8998, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        KnownCity("مكة المكرمة", "السعودية", 21.3891, 39.8579, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        KnownCity("المدينة المنورة", "السعودية", 24.5247, 39.5692, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        KnownCity("الرياض", "السعودية", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        KnownCity("جدة", "السعودية", 21.4858, 39.1925, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        KnownCity("دبي", "الإمارات", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.DUBAI),
        KnownCity("أبوظبي", "الإمارات", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.DUBAI),
        KnownCity("الدوحة", "قطر", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.QATAR),
        KnownCity("مدينة الكويت", "الكويت", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.KUWAIT),
        KnownCity("المنامة", "البحرين", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.UMM_AL_QURA),
        KnownCity("مسقط", "عُمان", 23.5880, 58.3829, "Asia/Muscat", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("عمّان", "الأردن", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("القدس", "فلسطين", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("بيروت", "لبنان", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("دمشق", "سوريا", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("بغداد", "العراق", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("الخرطوم", "السودان", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPTIAN),
        KnownCity("طرابلس", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.EGYPTIAN),
        KnownCity("تونس", "تونس", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("الجزائر", "الجزائر", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("الرباط", "المغرب", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("الدار البيضاء", "المغرب", 33.5731, -7.5898, "Africa/Casablanca", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("إسطنبول", "تركيا", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.TURKEY),
        KnownCity("لندن", "المملكة المتحدة", 51.5074, -0.1278, "Europe/London", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("نيويورك", "الولايات المتحدة", 40.7128, -74.0060, "America/New_York", CalculationMethod.NORTH_AMERICA),
        KnownCity("كراتشي", "باكستان", 24.8607, 67.0011, "Asia/Karachi", CalculationMethod.KARACHI),
        KnownCity("جاكرتا", "إندونيسيا", -6.2088, 106.8456, "Asia/Jakarta", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        KnownCity("كوالالمبور", "ماليزيا", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethod.SINGAPORE),
    )

    fun search(query: String): List<KnownCity> {
        val q = query.trim()
        return if (q.isEmpty()) all else all.filter { it.name.contains(q) || it.country.contains(q) }
    }
}
