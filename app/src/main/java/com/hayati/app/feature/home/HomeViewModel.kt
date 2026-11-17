package com.hayati.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.domain.model.OverviewEntry
import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val overview: List<OverviewEntry>,
)

/**
 * Only the tasks overview item has a real data source (today's tasks from the database).
 * Prayer, Quran and adhkar items stay [OverviewValue.Unavailable] until those features exist.
 */
class HomeViewModel(
    private val repository: TaskRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val day = MutableStateFlow(time.todayEpochDay())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = day
        .flatMapLatest { epochDay -> repository.observeTasksForDay(epochDay) }
        .map { tasks -> HomeUiState(overview = buildOverview(tasks)) }
        .stateIn(
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
