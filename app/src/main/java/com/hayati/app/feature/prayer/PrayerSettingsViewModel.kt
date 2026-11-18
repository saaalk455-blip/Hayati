package com.hayati.app.feature.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.data.prayer.location.LocationProvider
import com.hayati.app.data.prayer.location.LocationResult
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.KnownCity
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerNotificationConfig
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.repository.NotificationPermissionChecker
import com.hayati.app.domain.prayer.repository.PrayerAlarmScheduler
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import com.hayati.app.domain.prayer.usecase.ManualLocationError
import com.hayati.app.domain.prayer.usecase.ManualLocationResult
import com.hayati.app.domain.prayer.usecase.ManualLocationValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

enum class LocationMessage { PERMISSION_DENIED, SERVICES_DISABLED, UNAVAILABLE }

data class PrayerSettingsUiState(
    val settings: PrayerSettings? = null,
    val detecting: Boolean = false,
    val locationMessage: LocationMessage? = null,
    val manualErrors: Set<ManualLocationError> = emptySet(),
    /** False means alerts may arrive a few minutes late (exact alarms not allowed by the system). */
    val exactAlarmsAvailable: Boolean = true,
    val notificationsAllowed: Boolean = true,
    val notificationPermissionDenied: Boolean = false,
)

class PrayerSettingsViewModel(
    private val repository: PrayerSettingsRepository,
    private val locationProvider: LocationProvider,
    private val scheduler: PrayerAlarmScheduler,
    private val permission: NotificationPermissionChecker,
    private val deviceZone: () -> ZoneId = { ZoneId.systemDefault() },
) : ViewModel() {

    private data class Local(
        val detecting: Boolean = false,
        val message: LocationMessage? = null,
        val manualErrors: Set<ManualLocationError> = emptySet(),
        val exact: Boolean = true,
        val allowed: Boolean = true,
        val permissionDenied: Boolean = false,
    )

    private val local = MutableStateFlow(
        Local(exact = scheduler.canScheduleExact(), allowed = permission.areNotificationsAllowed()),
    )

    val uiState: StateFlow<PrayerSettingsUiState> = combine(repository.settings, local) { settings, l ->
        PrayerSettingsUiState(
            settings = settings,
            detecting = l.detecting,
            locationMessage = l.message,
            manualErrors = l.manualErrors,
            exactAlarmsAvailable = l.exact,
            notificationsAllowed = l.allowed,
            notificationPermissionDenied = l.permissionDenied,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), PrayerSettingsUiState())

    /** Re-reads system state the user may have changed in system settings. Call on resume. */
    fun refreshEnvironment() {
        local.update { it.copy(exact = scheduler.canScheduleExact(), allowed = permission.areNotificationsAllowed()) }
    }

    // ---- Location ----

    /** One explicit, user-requested lookup. Replaces the saved location only on success. */
    fun detectLocation() {
        if (local.value.detecting) return
        local.update { it.copy(detecting = true, message = null) }
        viewModelScope.launch {
            val result = try {
                locationProvider.requestCurrentLocation()
            } catch (failure: kotlinx.coroutines.CancellationException) {
                throw failure
            } catch (failure: Exception) {
                LocationResult.Unavailable
            }
            when (result) {
                is LocationResult.Success -> {
                    repository.update { s ->
                        s.copy(
                            location = PrayerLocation(
                                name = "",
                                country = "",
                                latitude = result.latitude,
                                longitude = result.longitude,
                                zoneId = deviceZone().id,
                                source = LocationSource.AUTOMATIC,
                            ),
                        )
                    }
                    local.update { it.copy(detecting = false, message = null) }
                }
                LocationResult.PermissionDenied -> finishDetect(LocationMessage.PERMISSION_DENIED)
                LocationResult.ServicesDisabled -> finishDetect(LocationMessage.SERVICES_DISABLED)
                LocationResult.Unavailable -> finishDetect(LocationMessage.UNAVAILABLE)
            }
        }
    }

    fun onLocationPermissionDenied() = local.update { it.copy(message = LocationMessage.PERMISSION_DENIED, detecting = false) }

    fun dismissLocationMessage() = local.update { it.copy(message = null) }

    private fun finishDetect(message: LocationMessage) = local.update { it.copy(detecting = false, message = message) }

    fun selectCity(city: KnownCity) {
        viewModelScope.launch {
            repository.update { s ->
                s.copy(
                    location = PrayerLocation(city.name, city.country, city.latitude, city.longitude, city.zoneId, LocationSource.MANUAL),
                    // A recommended method is a default only; it never overrides a user's own choice.
                    calculation = if (s.methodChosenByUser) s.calculation else s.calculation.copy(method = city.recommended),
                )
            }
        }
    }

    /** Returns true when the input was valid and has been saved. */
    fun saveManualLocation(name: String, country: String, latitude: String, longitude: String, zoneId: String): Boolean =
        when (val result = ManualLocationValidator.validate(name, country, latitude, longitude, zoneId)) {
            is ManualLocationResult.Invalid -> {
                local.update { it.copy(manualErrors = result.errors) }
                false
            }
            is ManualLocationResult.Valid -> {
                local.update { it.copy(manualErrors = emptySet()) }
                viewModelScope.launch { repository.update { it.copy(location = result.location) } }
                true
            }
        }

    fun clearManualErrors() = local.update { it.copy(manualErrors = emptySet()) }

    // ---- Calculation ----

    fun setMethod(method: CalculationMethod) = change {
        it.copy(calculation = it.calculation.copy(method = method), methodChosenByUser = true)
    }

    fun setAsr(asr: AsrMethod) = change { it.copy(calculation = it.calculation.copy(asr = asr)) }

    fun adjust(prayer: Prayer, deltaMinutes: Int) = change {
        val adjustments = it.calculation.adjustments
        it.copy(calculation = it.calculation.copy(adjustments = adjustments.with(prayer, adjustments.of(prayer) + deltaMinutes)))
    }

    // ---- Notifications ----

    /**
     * Turning notifications on is the user's consent. If no prayer was ever switched on, all five
     * are switched on so the master switch does something; the user can then fine-tune each one.
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        local.update { it.copy(permissionDenied = false, allowed = permission.areNotificationsAllowed()) }
        change { s ->
            val n = s.notifications
            val perPrayer = if (enabled && Prayer.entries.none { n.config(it).enabled }) {
                Prayer.entries.associateWith { n.config(it).copy(enabled = true) }
            } else {
                n.perPrayer
            }
            s.copy(notifications = n.copy(enabled = enabled, perPrayer = perPrayer))
        }
    }

    fun onNotificationPermissionDenied() = local.update { it.copy(permissionDenied = true, allowed = false) }

    fun setPrayerNotification(prayer: Prayer, enabled: Boolean) = updatePrayerConfig(prayer) { it.copy(enabled = enabled) }

    fun setReminder(prayer: Prayer, minutes: Int) {
        if (minutes in PrayerNotificationConfig.REMINDER_OPTIONS) updatePrayerConfig(prayer) { it.copy(reminderMinutes = minutes) }
    }

    fun setSound(prayer: Prayer, sound: NotificationSound) = updatePrayerConfig(prayer) { it.copy(sound = sound) }

    private fun updatePrayerConfig(prayer: Prayer, block: (PrayerNotificationConfig) -> PrayerNotificationConfig) = change { s ->
        s.copy(notifications = s.notifications.copy(perPrayer = s.notifications.perPrayer + (prayer to block(s.notifications.config(prayer)))))
    }

    // ---- Display ----

    fun setUse24Hour(value: Boolean) = change { it.copy(use24Hour = value) }

    fun adjustHijri(delta: Int) = change {
        it.copy(hijriOffsetDays = (it.hijriOffsetDays + delta).coerceIn(PrayerSettings.HIJRI_OFFSET_MIN, PrayerSettings.HIJRI_OFFSET_MAX))
    }

    private fun change(transform: (PrayerSettings) -> PrayerSettings) {
        viewModelScope.launch { repository.update(transform) }
    }
}
