package com.hayati.app.domain.prayer.usecase

import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.PrayerLocation

enum class ManualLocationError { LATITUDE, LONGITUDE, ZONE }

sealed interface ManualLocationResult {
    data class Valid(val location: PrayerLocation) : ManualLocationResult

    data class Invalid(val errors: Set<ManualLocationError>) : ManualLocationResult
}

/** Validates coordinates typed by the user (also accepts Arabic-Indic digits and decimal commas). */
object ManualLocationValidator {
    fun validate(
        name: String,
        country: String,
        latitude: String,
        longitude: String,
        zoneId: String,
    ): ManualLocationResult {
        val lat = parse(latitude)
        val lon = parse(longitude)
        val zone = zoneId.trim()
        val errors = buildSet {
            if (lat == null || !PrayerLocation.isValidLatitude(lat)) add(ManualLocationError.LATITUDE)
            if (lon == null || !PrayerLocation.isValidLongitude(lon)) add(ManualLocationError.LONGITUDE)
            if (!PrayerLocation.isValidZone(zone)) add(ManualLocationError.ZONE)
        }
        if (errors.isNotEmpty()) return ManualLocationResult.Invalid(errors)
        return ManualLocationResult.Valid(
            PrayerLocation(name.trim(), country.trim(), lat!!, lon!!, zone, LocationSource.MANUAL),
        )
    }

    fun parse(text: String): Double? {
        val normalized = buildString {
            text.trim().forEach { ch ->
                append(
                    when (ch) {
                        in '٠'..'٩' -> '0' + (ch - '٠')
                        in '۰'..'۹' -> '0' + (ch - '۰')
                        '٫', ',', '،' -> '.'
                        '−', '–' -> '-'
                        '٬' -> ""
                        else -> ch
                    },
                )
            }
        }
        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() }
    }
}
