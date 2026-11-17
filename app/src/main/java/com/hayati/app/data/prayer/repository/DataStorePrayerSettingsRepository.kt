package com.hayati.app.data.prayer.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NotificationSettings
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerAdjustments
import com.hayati.app.domain.prayer.model.PrayerCalculationSettings
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerNotificationConfig
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Prayer settings stored with Preferences DataStore (local file, never uploaded). */
class DataStorePrayerSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val defaultMethod: CalculationMethod,
) : PrayerSettingsRepository {

    override val settings: Flow<PrayerSettings> =
        dataStore.data.map { it.toSettings(defaultMethod) }.distinctUntilChanged()

    override suspend fun update(transform: (PrayerSettings) -> PrayerSettings) {
        dataStore.edit { prefs ->
            val current = prefs.toSettings(defaultMethod)
            val updated = transform(current)
            if (updated != current) prefs.write(updated)
        }
    }

    private companion object Keys {
        val LOC_NAME = stringPreferencesKey("loc_name")
        val LOC_COUNTRY = stringPreferencesKey("loc_country")
        val LOC_LAT = doublePreferencesKey("loc_lat")
        val LOC_LON = doublePreferencesKey("loc_lon")
        val LOC_ZONE = stringPreferencesKey("loc_zone")
        val LOC_SOURCE = stringPreferencesKey("loc_source")
        val METHOD = stringPreferencesKey("calc_method")
        val METHOD_USER = booleanPreferencesKey("calc_method_user")
        val ASR = stringPreferencesKey("calc_asr")
        val NOTIF_MASTER = booleanPreferencesKey("notif_master")
        val USE_24H = booleanPreferencesKey("use_24h")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")

        fun adjustment(p: Prayer) = intPreferencesKey("adj_${p.name.lowercase()}")
        fun notifEnabled(p: Prayer) = booleanPreferencesKey("notif_${p.name.lowercase()}_on")
        fun notifReminder(p: Prayer) = intPreferencesKey("notif_${p.name.lowercase()}_reminder")
        fun notifSound(p: Prayer) = stringPreferencesKey("notif_${p.name.lowercase()}_sound")

        inline fun <reified E : Enum<E>> String?.toEnum(fallback: E): E =
            enumValues<E>().firstOrNull { it.name == this } ?: fallback
    }

    private fun Preferences.toSettings(defaultMethod: CalculationMethod): PrayerSettings {
        val lat = this[LOC_LAT]
        val lon = this[LOC_LON]
        val zone = this[LOC_ZONE]
        val location = if (lat != null && lon != null && zone != null) {
            PrayerLocation(
                name = this[LOC_NAME].orEmpty(),
                country = this[LOC_COUNTRY].orEmpty(),
                latitude = lat,
                longitude = lon,
                zoneId = zone,
                source = this[LOC_SOURCE].toEnum(LocationSource.MANUAL),
            )
        } else {
            null
        }
        return PrayerSettings(
            location = location,
            calculation = PrayerCalculationSettings(
                method = this[METHOD].toEnum(defaultMethod),
                asr = this[ASR].toEnum(AsrMethod.STANDARD),
                adjustments = PrayerAdjustments(
                    Prayer.entries.associateWith { (this[adjustment(it)] ?: 0) },
                ),
            ),
            methodChosenByUser = this[METHOD_USER] ?: false,
            notifications = NotificationSettings(
                enabled = this[NOTIF_MASTER] ?: false,
                perPrayer = Prayer.entries.associateWith { prayer ->
                    PrayerNotificationConfig(
                        enabled = this[notifEnabled(prayer)] ?: false,
                        reminderMinutes = (this[notifReminder(prayer)] ?: 0)
                            .takeIf { it in PrayerNotificationConfig.REMINDER_OPTIONS } ?: 0,
                        sound = this[notifSound(prayer)].toEnum(NotificationSound.DEFAULT),
                    )
                },
            ),
            use24Hour = this[USE_24H] ?: false,
            hijriOffsetDays = (this[HIJRI_OFFSET] ?: 0)
                .coerceIn(PrayerSettings.HIJRI_OFFSET_MIN, PrayerSettings.HIJRI_OFFSET_MAX),
        )
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.write(s: PrayerSettings) {
        val loc = s.location
        if (loc == null) {
            remove(LOC_NAME); remove(LOC_COUNTRY); remove(LOC_LAT); remove(LOC_LON)
            remove(LOC_ZONE); remove(LOC_SOURCE)
        } else {
            this[LOC_NAME] = loc.name
            this[LOC_COUNTRY] = loc.country
            this[LOC_LAT] = loc.latitude
            this[LOC_LON] = loc.longitude
            this[LOC_ZONE] = loc.zoneId
            this[LOC_SOURCE] = loc.source.name
        }
        this[METHOD] = s.calculation.method.name
        this[METHOD_USER] = s.methodChosenByUser
        this[ASR] = s.calculation.asr.name
        Prayer.entries.forEach { this[adjustment(it)] = s.calculation.adjustments.of(it) }
        this[NOTIF_MASTER] = s.notifications.enabled
        Prayer.entries.forEach { prayer ->
            val config = s.notifications.config(prayer)
            this[notifEnabled(prayer)] = config.enabled
            this[notifReminder(prayer)] = config.reminderMinutes
            this[notifSound(prayer)] = config.sound.name
        }
        this[USE_24H] = s.use24Hour
        this[HIJRI_OFFSET] = s.hijriOffsetDays.coerceIn(PrayerSettings.HIJRI_OFFSET_MIN, PrayerSettings.HIJRI_OFFSET_MAX)
    }
}
