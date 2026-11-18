package com.hayati.app.feature.home

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeProvider
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeState
import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import com.hayati.app.testing.FakePrayerLogRepository
import com.hayati.app.testing.FakePrayerSettingsRepository
import com.hayati.app.testing.FakePrayerTimeCalculator
import com.hayati.app.testing.FakeAdhkarContentSource
import com.hayati.app.testing.FakeAdhkarProgressRepository
import com.hayati.app.testing.FakeTaskRepository
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

    private val prayerSettings = FakePrayerSettingsRepository(PrayerSettings.default(CalculationMethod.EGYPTIAN))
    private val prayerLogs = FakePrayerLogRepository()
    private val prayerProvider = PrayerSnapshotProvider(prayerSettings, prayerLogs, FakePrayerTimeCalculator())

    // 2026-06-15 04:00 UTC: after the fake Fajr (03:00), before Dhuhr (10:00).
    private val prayerNow = java.time.Instant.parse("2026-06-15T04:00:00Z").toEpochMilli()

    private val adhkarContent = FakeAdhkarContentSource(
        listOf(
            Dhikr("m1", AdhkarCategory.MORNING, "نص اختبار ١", 1, ""),
            Dhikr("m2", AdhkarCategory.MORNING, "نص اختبار ٢", 1, ""),
        ),
    )
    private val adhkarProgress = FakeAdhkarProgressRepository()

    private fun newViewModel() = HomeViewModel(
        repository, time, prayerProvider, AdhkarHomeProvider(adhkarContent, adhkarProgress),
        ticker = flowOf(prayerNow),
    )

    private fun TestScope.createViewModel(): HomeViewModel {
        val viewModel = newViewModel()
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
            OverviewItem.QuranWird,
        )
        assertTrue(unavailable.all { viewModel.valueOf(it) == OverviewValue.Unavailable })
    }

    @Test
    fun adhkar_card_shows_real_progress_and_follows_the_counters() = runTest(dispatcher) {
        val viewModel = createViewModel()
        // prayerNow is 04:00 UTC; the provider uses the device zone, so only check the shape here.
        val first = viewModel.uiState.value.adhkar
        assertTrue(first is AdhkarHomeState.Ready)
        assertEquals(0, (first as AdhkarHomeState.Ready).doneCount)
        assertEquals(2, first.total)

        adhkarProgress.setCount(100L, first.category, "m1", 1)
        val after = viewModel.uiState.value.adhkar as AdhkarHomeState.Ready
        assertEquals(1, after.doneCount)
    }

    @Test
    fun adhkar_card_is_unavailable_when_there_is_no_content() = runTest(dispatcher) {
        adhkarContent.adhkar = emptyList()
        val viewModel = createViewModel()
        assertEquals(AdhkarHomeState.Unavailable, viewModel.uiState.value.adhkar)
    }

    @Test
    fun adhkar_card_moves_to_the_new_day_with_zero_progress() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val category = (viewModel.uiState.value.adhkar as AdhkarHomeState.Ready).category
        adhkarProgress.setCount(100L, category, "m1", 1)
        adhkarProgress.setCount(100L, category, "m2", 1)
        assertTrue((viewModel.uiState.value.adhkar as AdhkarHomeState.Ready).isComplete)
        time.today = 101L
        viewModel.refreshDay()
        assertEquals(0, (viewModel.uiState.value.adhkar as AdhkarHomeState.Ready).doneCount)
    }

    @Test
    fun before_any_data_arrives_every_item_is_unavailable() {
        val viewModel = newViewModel()

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

    @Test
    fun prayer_is_not_configured_until_a_location_exists() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertEquals(PrayerSnapshot.NotConfigured, viewModel.uiState.value.prayer)
    }

    @Test
    fun prayer_card_uses_real_calculated_data_once_a_location_is_set() = runTest(dispatcher) {
        prayerSettings.update {
            it.copy(location = PrayerLocation("مدينة", "بلد", 10.0, 10.0, "UTC", LocationSource.MANUAL))
        }
        prayerLogs.setCompleted(java.time.LocalDate.of(2026, 6, 15).toEpochDay(), Prayer.FAJR, true)
        val viewModel = createViewModel()

        val prayer = viewModel.uiState.value.prayer as PrayerSnapshot.Ready
        assertEquals(Prayer.DHUHR, prayer.next?.prayer)
        assertEquals(6 * 60 * 60 * 1000L, prayer.remainingMillis)
        assertEquals(1, prayer.summary.completed)
    }
}
