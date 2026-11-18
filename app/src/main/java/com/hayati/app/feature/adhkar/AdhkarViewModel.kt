package com.hayati.app.feature.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.AdhkarCounter
import com.hayati.app.domain.adhkar.model.CategoryProgress
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.model.DhikrProgress
import com.hayati.app.domain.adhkar.repository.AdhkarContentSource
import com.hayati.app.domain.adhkar.repository.AdhkarProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AdhkarStatus { LOADING, ERROR, EMPTY, READY }

data class AdhkarUiState(
    val status: AdhkarStatus = AdhkarStatus.LOADING,
    val selected: AdhkarCategory = AdhkarCategory.MORNING,
    val categories: List<CategoryProgress> = emptyList(),
    val currentIndex: Int = 0,
    val saveFailed: Boolean = false,
) {
    val selectedProgress: CategoryProgress? get() = categories.firstOrNull { it.category == selected }
    val current: DhikrProgress? get() = selectedProgress?.items?.getOrNull(currentIndex)
}

private sealed interface Content {
    data object Loading : Content
    data object Failed : Content
    data class Loaded(val adhkar: List<Dhikr>) : Content
}

/**
 * Shows one dhikr at a time with a counter. The "current" dhikr is the first unfinished one unless
 * the user navigated manually. Counts live in Room per local day, so a new day starts at zero.
 */
class AdhkarViewModel(
    private val content: AdhkarContentSource,
    private val progress: AdhkarProgressRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val day = MutableStateFlow(time.todayEpochDay())
    private val selected = MutableStateFlow(AdhkarCategory.MORNING)
    private val manualIndex = MutableStateFlow<Int?>(null)
    private val saveFailed = MutableStateFlow(false)
    private val loaded = MutableStateFlow<Content>(Content.Loading)

    init {
        viewModelScope.launch {
            loaded.value = try {
                Content.Loaded(content.load())
            } catch (e: Exception) {
                Content.Failed
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val counts: Flow<Map<AdhkarCategory, Map<String, Int>>> = day.flatMapLatest { epochDay ->
        val categories = AdhkarCategory.entries
        combine(categories.map { progress.observeCounts(epochDay, it) }) { maps ->
            categories.zip(maps.toList()).toMap()
        }
    }

    val uiState: StateFlow<AdhkarUiState> = combine(
        loaded, counts, selected, manualIndex, saveFailed,
    ) { content, counts, selected, manual, failed ->
        when (content) {
            Content.Loading -> AdhkarUiState(AdhkarStatus.LOADING, selected)
            Content.Failed -> AdhkarUiState(AdhkarStatus.ERROR, selected)
            is Content.Loaded -> {
                if (content.adhkar.isEmpty()) return@combine AdhkarUiState(AdhkarStatus.EMPTY, selected)
                val categories = AdhkarCategory.entries.map { category ->
                    val saved = counts[category].orEmpty()
                    CategoryProgress(
                        category = category,
                        items = content.adhkar.filter { it.category == category }.map { dhikr ->
                            DhikrProgress(dhikr, AdhkarCounter.clamp(saved[dhikr.id] ?: 0, dhikr.repeat))
                        },
                    )
                }
                val items = categories.first { it.category == selected }.items
                val auto = items.indexOfFirst { !it.isDone }.takeIf { it >= 0 } ?: (items.size - 1).coerceAtLeast(0)
                AdhkarUiState(
                    status = AdhkarStatus.READY,
                    selected = selected,
                    categories = categories,
                    currentIndex = manual?.coerceIn(0, (items.size - 1).coerceAtLeast(0)) ?: auto,
                    saveFailed = failed,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AdhkarUiState())

    fun refreshDay() {
        day.value = time.todayEpochDay()
    }

    fun select(category: AdhkarCategory) {
        selected.value = category
        manualIndex.value = null
    }

    /** One recitation of the current dhikr. A finished dhikr ignores further taps. */
    fun tap() = change { current -> AdhkarCounter.increment(current.count, current.dhikr.repeat) }

    /** Undo one recitation (tapped by mistake). Never goes below zero. */
    fun undo() = change { current -> AdhkarCounter.decrement(current.count, current.dhikr.repeat) }

    fun next() = move(+1)

    fun previous() = move(-1)

    fun dismissError() {
        saveFailed.value = false
    }

    private fun move(delta: Int) {
        val state = uiState.value
        val size = state.selectedProgress?.items?.size ?: return
        manualIndex.value = (state.currentIndex + delta).coerceIn(0, (size - 1).coerceAtLeast(0))
    }

    private fun change(newCount: (DhikrProgress) -> Int) {
        val state = uiState.value
        val current = state.current ?: return
        val updated = newCount(current)
        if (updated == current.count) return
        viewModelScope.launch {
            try {
                progress.setCount(day.value, state.selected, current.dhikr.id, updated)
                saveFailed.value = false
            } catch (e: Exception) {
                saveFailed.value = true
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
