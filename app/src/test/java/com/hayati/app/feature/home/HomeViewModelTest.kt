package com.hayati.app.feature.home

import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun overview_lists_every_item_in_declared_order() {
        val state = HomeViewModel().uiState.value

        assertEquals(OverviewItem.entries.toList(), state.overview.map { it.item })
    }

    @Test
    fun overview_values_are_unavailable_because_no_data_source_exists() {
        val state = HomeViewModel().uiState.value

        assertTrue(state.overview.all { it.value == OverviewValue.Unavailable })
    }
}
