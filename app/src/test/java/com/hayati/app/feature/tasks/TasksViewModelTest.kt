package com.hayati.app.feature.tasks

import com.hayati.app.domain.model.TaskTitle
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

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

    /** Creates the ViewModel and keeps `uiState` collected, like the UI does while visible. */
    private fun TestScope.createViewModel(): TasksViewModel {
        val viewModel = TasksViewModel(repository, time)
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect { } }
        return viewModel
    }

    @Test
    fun starts_empty_and_finished_loading() = runTest(dispatcher) {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.tasks.isEmpty())
        assertEquals("", state.newTitle)
    }

    @Test
    fun addTask_saves_a_task_for_today_and_clears_the_input() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onNewTitleChange("  قراءة  ")
        viewModel.addTask()

        val state = viewModel.uiState.value
        assertEquals(listOf("قراءة"), state.tasks.map { it.title })
        assertEquals(100L, state.tasks.single().epochDay)
        assertEquals("", state.newTitle)
        assertFalse(state.showNewTitleError)
    }

    @Test
    fun addTask_with_blank_input_shows_an_error_and_saves_nothing() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onNewTitleChange("   ")
        viewModel.addTask()

        val state = viewModel.uiState.value
        assertTrue(state.showNewTitleError)
        assertTrue(state.tasks.isEmpty())
        assertTrue(repository.allTasks.isEmpty())
    }

    @Test
    fun typing_clears_the_input_error() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.addTask()
        assertTrue(viewModel.uiState.value.showNewTitleError)

        viewModel.onNewTitleChange("م")

        assertFalse(viewModel.uiState.value.showNewTitleError)
    }

    @Test
    fun input_is_limited_to_the_maximum_title_length() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onNewTitleChange("x".repeat(TaskTitle.MAX_LENGTH + 30))

        assertEquals(TaskTitle.MAX_LENGTH, viewModel.uiState.value.newTitle.length)
    }

    @Test
    fun toggleCompleted_marks_the_task_done_and_counts_it() = runTest(dispatcher) {
        repository.seed("أ", epochDay = 100L)
        repository.seed("ب", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.toggleCompleted(viewModel.uiState.value.tasks.first())

        val state = viewModel.uiState.value
        assertEquals(1, state.completedCount)
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun toggleCompleted_twice_reopens_the_task() = runTest(dispatcher) {
        val seeded = repository.seed("أ", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.toggleCompleted(seeded)
        viewModel.toggleCompleted(viewModel.uiState.value.tasks.single())

        assertEquals(0, viewModel.uiState.value.completedCount)
    }

    @Test
    fun editing_flow_updates_the_title() = runTest(dispatcher) {
        val seeded = repository.seed("قديم", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.startEditing(seeded)
        assertEquals("قديم", viewModel.uiState.value.editing?.text)

        viewModel.onEditTextChange("جديد")
        viewModel.confirmEdit()

        val state = viewModel.uiState.value
        assertNull(state.editing)
        assertEquals("جديد", state.tasks.single().title)
    }

    @Test
    fun editing_with_blank_text_keeps_the_dialog_open_with_an_error() = runTest(dispatcher) {
        val seeded = repository.seed("قديم", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.startEditing(seeded)
        viewModel.onEditTextChange("  ")
        viewModel.confirmEdit()

        val state = viewModel.uiState.value
        assertNotNull(state.editing)
        assertTrue(state.editing!!.showError)
        assertEquals("قديم", state.tasks.single().title)
    }

    @Test
    fun cancelEdit_closes_the_dialog_without_changing_the_task() = runTest(dispatcher) {
        val seeded = repository.seed("قديم", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.startEditing(seeded)
        viewModel.onEditTextChange("تغيير")
        viewModel.cancelEdit()

        val state = viewModel.uiState.value
        assertNull(state.editing)
        assertEquals("قديم", state.tasks.single().title)
    }

    @Test
    fun delete_requires_confirmation() = runTest(dispatcher) {
        val seeded = repository.seed("احذفني", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.requestDelete(seeded)
        assertEquals(seeded, viewModel.uiState.value.pendingDelete)
        assertEquals(1, viewModel.uiState.value.tasks.size)

        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertNull(state.pendingDelete)
        assertTrue(state.tasks.isEmpty())
    }

    @Test
    fun cancelDelete_keeps_the_task() = runTest(dispatcher) {
        val seeded = repository.seed("ابقَ", epochDay = 100L)
        val viewModel = createViewModel()

        viewModel.requestDelete(seeded)
        viewModel.cancelDelete()

        val state = viewModel.uiState.value
        assertNull(state.pendingDelete)
        assertEquals(1, state.tasks.size)
    }

    @Test
    fun only_todays_tasks_are_shown_and_refreshDay_follows_the_calendar() = runTest(dispatcher) {
        repository.seed("اليوم", epochDay = 100L)
        repository.seed("غدًا", epochDay = 101L)
        val viewModel = createViewModel()
        assertEquals(listOf("اليوم"), viewModel.uiState.value.tasks.map { it.title })

        time.today = 101L
        viewModel.refreshDay()

        assertEquals(listOf("غدًا"), viewModel.uiState.value.tasks.map { it.title })
    }

    @Test
    fun a_storage_failure_is_reported_instead_of_crashing_and_can_be_dismissed() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            repository.failure = IllegalStateException("disk full")

            viewModel.onNewTitleChange("مهمة")
            viewModel.addTask()

            assertTrue(viewModel.uiState.value.operationFailed)
            assertEquals("مهمة", viewModel.uiState.value.newTitle)

            viewModel.dismissError()
            assertFalse(viewModel.uiState.value.operationFailed)
        }
}
