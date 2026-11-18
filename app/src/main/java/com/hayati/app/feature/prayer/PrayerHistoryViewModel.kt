package com.hayati.app.feature.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.repository.PrayerLogRepository
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import com.hayati.app.domain.prayer.usecase.HistoryBuilder
import com.hayati.app.domain.prayer.usecase.HistoryDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId

data class PrayerHistoryUiState(
    val loaded: Boolean = false,
    val days: List<HistoryDay> = emptyList(),
    val todayEpochDay: Long = 0,
)

/** Last [DAYS] prayer days (in the prayer location's zone) that contain recorded prayers. */
@OptIn(ExperimentalCoroutinesApi::class)
class PrayerHistoryViewModel(
    settingsRepository: PrayerSettingsRepository,
    logRepository: PrayerLogRepository,
    private val time: TimeProvider,
) : ViewModel() {

    val uiState: StateFlow<PrayerHistoryUiState> = settingsRepository.settings
        .map { s -> s.location?.takeIf { PrayerLocation.isValidZone(it.zoneId) }?.zone ?: ZoneId.systemDefault() }
        .distinctUntilChanged()
        .flatMapLatest { zone ->
            val today = Instant.ofEpochMilli(time.nowMillis()).atZone(zone).toLocalDate().toEpochDay()
            logRepository.observeRange(today - (DAYS - 1), today).map { logs ->
                PrayerHistoryUiState(loaded = true, days = HistoryBuilder.build(logs), todayEpochDay = today)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), PrayerHistoryUiState())

    companion object {
        const val DAYS = 30
    }
}
