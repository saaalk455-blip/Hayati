package com.hayati.app.feature.home

import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import com.hayati.app.testing.FakeTaskRepository
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var time: FakeTimeProvider
    private lateinit var repository: FakeTaskRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        time = FakeTimeProvider(today = 100L)
        repository = FakeTaskRepository(time)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): HomeViewModel {
        val viewModel = HomeViewModel(repository, time)
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect { } }
        return viewModel
    }

    private fun HomeViewModel.valueOf(item: OverviewItem): OverviewValue =
        uiState.value.overview.first { it.item == item }.value

    @Test
    fun overview_lists_every_item_in_declared_order() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertEquals(
            OverviewItem.entries.toList(),
            viewModel.uiState.value.overview.map { it.item },
        )
    }

    @Test
    fun items_without_a_data_source_stay_unavailable() = runTest(dispatcher) {
        repository.seed("مهمة", epochDay = 100L)
        val viewModel = createViewModel()

        val unavailable = listOf(
            OverviewItem.NextPrayer,
            OverviewItem.TimeRemaining,
            OverviewItem.QuranWird,
            OverviewItem.Adhkar,
        )
        assertTrue(unavailable.all { viewModel.valueOf(it) == OverviewValue.Unavailable })
    }

    @Test
    fun before_any_data_arrives_every_item_is_unavailable() {
        val viewModel = HomeViewModel(repository, time)

        assertTrue(viewModel.uiState.value.overview.all { it.value == OverviewValue.Unavailable })
    }

    @Test
    fun tasks_item_shows_zero_of_zero_when_there_are_no_tasks_today() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertEquals(OverviewValue.TasksProgress(0, 0), viewModel.valueOf(OverviewItem.Tasks))
    }

    @Test
    fun tasks_item_counts_only_todays_tasks_and_follows_changes() = runTest(dispatcher) {
        repository.seed("منجزة", epochDay = 100L, completed = true)
        val open = repository.seed("مفتوحة", epochDay = 100L)
        repository.seed("أمس", epochDay = 99L, completed = true)
        val viewModel = createViewModel()

        assertEquals(OverviewValue.TasksProgress(1, 2), viewModel.valueOf(OverviewItem.Tasks))

        repository.setCompleted(open.id, completed = true)

        assertEquals(OverviewValue.TasksProgress(2, 2), viewModel.valueOf(OverviewItem.Tasks))
    }

    @Test
    fun refreshDay_moves_the_overview_to_the_new_day() = runTest(dispatcher) {
        repository.seed("اليوم", epochDay = 100L)
        val viewModel = createViewModel()
        assertEquals(OverviewValue.TasksProgress(0, 1), viewModel.valueOf(OverviewItem.Tasks))

        time.today = 101L
        viewModel.refreshDay()

        assertEquals(OverviewValue.TasksProgress(0, 0), viewModel.valueOf(OverviewItem.Tasks))
    }
}
