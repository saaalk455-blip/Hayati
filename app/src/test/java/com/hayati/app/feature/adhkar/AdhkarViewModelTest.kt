package com.hayati.app.feature.adhkar

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.testing.FakeAdhkarContentSource
import com.hayati.app.testing.FakeAdhkarProgressRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Test-only adhkar (obvious placeholders): these tests check counting and navigation rules. */
@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val morning1 = Dhikr("m1", AdhkarCategory.MORNING, "اختبار صباح ١", 2, "")
    private val morning2 = Dhikr("m2", AdhkarCategory.MORNING, "اختبار صباح ٢", 1, "مرجع")
    private val evening1 = Dhikr("e1", AdhkarCategory.EVENING, "اختبار مساء ١", 3, "")

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.vm(
        content: FakeAdhkarContentSource = FakeAdhkarContentSource(listOf(morning1, morning2, evening1)),
        progress: FakeAdhkarProgressRepository = FakeAdhkarProgressRepository(),
        time: FakeTimeProvider = FakeTimeProvider(today = 500L),
    ): AdhkarViewModel {
        val viewModel = AdhkarViewModel(content, progress, time)
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `starts on the first unfinished morning dhikr`() = runTest {
        val state = vm().uiState.value
        assertEquals(AdhkarStatus.READY, state.status)
        assertEquals(AdhkarCategory.MORNING, state.selected)
        assertEquals("m1", state.current?.dhikr?.id)
        assertEquals(0, state.current?.count)
    }

    @Test
    fun `no content shows the empty state and never invents adhkar`() = runTest {
        val state = vm(content = FakeAdhkarContentSource(emptyList())).uiState.value
        assertEquals(AdhkarStatus.EMPTY, state.status)
        assertNull(state.current)
    }

    @Test
    fun `an unreadable content file shows the error state`() = runTest {
        val state = vm(content = FakeAdhkarContentSource(failure = IllegalStateException("bad"))).uiState.value
        assertEquals(AdhkarStatus.ERROR, state.status)
        assertTrue(state.categories.isEmpty())
    }

    @Test
    fun `tapping counts up, finishing a dhikr moves on to the next unfinished one`() = runTest {
        val viewModel = vm()
        viewModel.tap()
        assertEquals(1, viewModel.uiState.value.current?.count)
        assertEquals("m1", viewModel.uiState.value.current?.dhikr?.id)
        viewModel.tap()
        assertEquals("m2", viewModel.uiState.value.current?.dhikr?.id)
        assertEquals(1, viewModel.uiState.value.selectedProgress?.doneCount)
    }

    @Test
    fun `finishing every dhikr completes the category and extra taps change nothing`() = runTest {
        val progress = FakeAdhkarProgressRepository()
        val viewModel = vm(progress = progress)
        repeat(3) { viewModel.tap() }
        val state = viewModel.uiState.value
        assertTrue(state.selectedProgress!!.isComplete)
        viewModel.tap()
        assertEquals(3, viewModel.uiState.value.selectedProgress!!.items.sumOf { it.count })
    }

    @Test
    fun `undo lowers the count and stops at zero`() = runTest {
        val viewModel = vm()
        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.current?.count)
        viewModel.tap()
        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.current?.count)
    }

    @Test
    fun `manual navigation is bounded and tapping counts the dhikr you moved to`() = runTest {
        val viewModel = vm()
        viewModel.previous()
        assertEquals(0, viewModel.uiState.value.currentIndex)
        viewModel.next()
        viewModel.next()
        assertEquals(1, viewModel.uiState.value.currentIndex)
        viewModel.tap()
        assertEquals(1, viewModel.uiState.value.categories.first().items[1].count)
        assertEquals(0, viewModel.uiState.value.categories.first().items[0].count)
    }

    @Test
    fun `categories keep separate counters`() = runTest {
        val viewModel = vm()
        viewModel.tap()
        viewModel.select(AdhkarCategory.EVENING)
        assertEquals("e1", viewModel.uiState.value.current?.dhikr?.id)
        assertEquals(0, viewModel.uiState.value.current?.count)
        viewModel.select(AdhkarCategory.MORNING)
        assertEquals(1, viewModel.uiState.value.current?.count)
    }

    @Test
    fun `a category with no adhkar has no current item`() = runTest {
        val viewModel = vm()
        viewModel.select(AdhkarCategory.AFTER_PRAYER)
        assertNull(viewModel.uiState.value.current)
        assertEquals(0, viewModel.uiState.value.selectedProgress?.total)
    }

    @Test
    fun `a new day starts from zero and keeps yesterday's counts`() = runTest {
        val time = FakeTimeProvider(today = 500L)
        val progress = FakeAdhkarProgressRepository()
        val viewModel = vm(progress = progress, time = time)
        viewModel.tap()
        time.today = 501L
        viewModel.refreshDay()
        assertEquals(0, viewModel.uiState.value.current?.count)
        time.today = 500L
        viewModel.refreshDay()
        assertEquals(1, viewModel.uiState.value.current?.count)
    }

    @Test
    fun `a storage failure is reported and can be dismissed`() = runTest {
        val progress = FakeAdhkarProgressRepository().apply { failure = IllegalStateException("disk") }
        val viewModel = vm(progress = progress)
        viewModel.tap()
        assertTrue(viewModel.uiState.value.saveFailed)
        assertEquals(0, viewModel.uiState.value.current?.count)
        viewModel.dismissError()
        assertFalse(viewModel.uiState.value.saveFailed)
    }
}
