package com.hayati.app.feature.home

import androidx.lifecycle.ViewModel
import com.hayati.app.domain.model.OverviewEntry
import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val overview: List<OverviewEntry>,
)

/**
 * Phase 2: no prayer, Quran, adhkar or task data source exists, so every overview item is
 * explicitly [OverviewValue.Unavailable]. Later phases replace these values with real data.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            overview = OverviewItem.entries.map { item ->
                OverviewEntry(item = item, value = OverviewValue.Unavailable)
            },
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
