package com.hayati.app.feature.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.core.common.clockTicker
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.repository.PrayerLogRepository
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Prayer screen state. The snapshot (and the per-second countdown) only runs while the screen
 * collects [uiState] with the lifecycle; after the app returns from the background the next tick
 * reads the real clock, so the countdown is immediately correct.
 */
class PrayerViewModel(
    snapshotProvider: PrayerSnapshotProvider,
    private val logRepository: PrayerLogRepository,
    private val time: TimeProvider,
    ticker: Flow<Long> = clockTicker(time),
) : ViewModel() {

    private val sharedTicker = ticker.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        replay = 1,
    )

    val uiState: StateFlow<PrayerSnapshot> = snapshotProvider.observe(sharedTicker)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PrayerSnapshot.Loading)

    private val _saveFailed = MutableStateFlow(false)
    val saveFailed: StateFlow<Boolean> = _saveFailed

    fun dismissError() {
        _saveFailed.value = false
    }

    /**
     * Records or reverts a prayer. Recording is only possible once the prayer's time has come:
     * the user decides, but a prayer that has not started cannot already be prayed.
     */
    fun setCompleted(prayer: Prayer, completed: Boolean) {
        val snapshot = uiState.value as? PrayerSnapshot.Ready ?: return
        if (completed) {
            val row = snapshot.rows.first { it.event == PrayerEvent.of(prayer) }
            if (row.time.toEpochMilli() > time.nowMillis()) return
        }
        viewModelScope.launch {
            try {
                logRepository.setCompleted(snapshot.date.toEpochDay(), prayer, completed)
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: Exception) {
                _saveFailed.value = true
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
