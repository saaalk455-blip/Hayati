package com.hayati.app.data.prayer.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Uses the framework [LocationManager] (no Google Play services, nothing leaves the device) and
 * only the coarse-location permission: prayer times do not need a precise position, and coarse
 * uses less power. Background location is never requested.
 */
class AndroidLocationProvider(context: Context) : LocationProvider {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun requestCurrentLocation(): LocationResult {
        val granted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return LocationResult.PermissionDenied
        if (!LocationManagerCompat.isLocationEnabled(manager)) return LocationResult.ServicesDisabled

        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return LocationResult.ServicesDisabled

        for (provider in providers) {
            val location = withTimeoutOrNull(FIX_TIMEOUT_MILLIS) { currentFix(provider) }
            if (location != null) return LocationResult.Success(location.latitude, location.longitude)
        }
        // Fall back to a recent cached position rather than failing.
        val cached = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        return if (cached != null) {
            LocationResult.Success(cached.latitude, cached.longitude)
        } else {
            LocationResult.Unavailable
        }
    }

    @Suppress("MissingPermission")
    private suspend fun currentFix(provider: String): Location? = suspendCancellableCoroutine { cont ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(provider, signal, appContext.mainExecutor) { location ->
                    if (cont.isActive) cont.resume(location)
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (cont.isActive) cont.resume(location)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit
                }
                cont.invokeOnCancellation { manager.removeUpdates(listener) }
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }
        } catch (failure: SecurityException) {
            if (cont.isActive) cont.resume(null)
        } catch (failure: IllegalArgumentException) {
            if (cont.isActive) cont.resume(null)
        }
    }

    private companion object {
        const val FIX_TIMEOUT_MILLIS = 15_000L
    }
}
