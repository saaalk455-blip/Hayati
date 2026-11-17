package com.hayati.app.domain.prayer.model

import java.time.DateTimeException
import java.time.ZoneId

enum class LocationSource { AUTOMATIC, MANUAL }

/**
 * The only location data Hayati keeps: what is needed to compute prayer times locally.
 * [zoneId] is a real IANA zone id (never a fixed UTC offset) so daylight saving is handled.
 */
data class PrayerLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val source: LocationSource,
) {
    val zone: ZoneId get() = ZoneId.of(zoneId)

    companion object {
        fun isValidLatitude(value: Double) = value.isFinite() && value in -90.0..90.0

        fun isValidLongitude(value: Double) = value.isFinite() && value in -180.0..180.0

        fun isValidZone(id: String): Boolean = try {
            ZoneId.of(id.trim())
            id.isNotBlank()
        } catch (failure: DateTimeException) {
            false
        }
    }
}
