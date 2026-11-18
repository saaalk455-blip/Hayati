package com.hayati.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.core.common.clockTicker
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeProvider
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeState
import com.hayati.app.domain.model.OverviewEntry
import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val overview: List<OverviewEntry>,
    /** Real prayer data from the same provider the Prayer screen uses. */
    val prayer: PrayerSnapshot = PrayerSnapshot.Loading,
    /** Real adhkar progress from the same source as the Adhkar screen. */
    val adhkar: AdhkarHomeState = AdhkarHomeState.Loading,
)

/**
 * Tasks come from the task database and prayer information from the shared
 * [PrayerSnapshotProvider]. Adhkar progress comes from the shared [AdhkarHomeProvider]. The Quran wird stays
 * [OverviewValue.Unavailable] until it exists.
 */
class HomeViewModel(
    private val repository: TaskRepository,
    private val time: TimeProvider,
    prayerProvider: PrayerSnapshotProvider,
    adhkarProvider: AdhkarHomeProvider,
    ticker: Flow<Long> = clockTicker(time),
) : ViewModel() {

    private val day = MutableStateFlow(time.todayEpochDay())

    private val sharedTicker = ticker.shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasks: Flow<List<Task>> = day.flatMapLatest { epochDay -> repository.observeTasksForDay(epochDay) }

    val uiState: StateFlow<HomeUiState> = combine(
        tasks,
        prayerProvider.observe(sharedTicker),
        adhkarProvider.observe(day, sharedTicker),
    ) { tasks, prayer, adhkar ->
        HomeUiState(overview = buildOverview(tasks), prayer = prayer, adhkar = adhkar)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(overview = buildOverview(tasks = null)),
    )

    /** Call when the screen becomes visible so the overview follows the calendar day. */
    fun refreshDay() {
        day.value = time.todayEpochDay()
    }

    private fun buildOverview(tasks: List<Task>?): List<OverviewEntry> =
        OverviewItem.entries.map { item ->
            val value = if (item == OverviewItem.Tasks && tasks != null) {
                OverviewValue.TasksProgress(
                    completed = tasks.count { it.isCompleted },
                    total = tasks.size,
                )
            } else {
                OverviewValue.Unavailable
            }
            OverviewEntry(item = item, value = value)
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
