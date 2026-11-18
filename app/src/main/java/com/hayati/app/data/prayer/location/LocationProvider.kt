package com.hayati.app.data.prayer.location

/** Result of one explicit, user-requested location lookup. */
sealed interface LocationResult {
    data class Success(val latitude: Double, val longitude: Double) : LocationResult

    data object PermissionDenied : LocationResult

    data object ServicesDisabled : LocationResult

    /** No provider returned a position in time (no signal, no cached fix). */
    data object Unavailable : LocationResult
}

interface LocationProvider {
    /** One low-power request. Never runs in the background and never repeats on its own. */
    suspend fun requestCurrentLocation(): LocationResult
}
