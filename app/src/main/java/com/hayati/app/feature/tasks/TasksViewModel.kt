package com.hayati.app.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.model.TaskTitle
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditingTask(
    val id: Long,
    val text: String,
    val showError: Boolean = false,
)

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val newTitle: String = "",
    val showNewTitleError: Boolean = false,
    val editing: EditingTask? = null,
    val pendingDelete: Task? = null,
    val operationFailed: Boolean = false,
) {
    val completedCount: Int get() = tasks.count { it.isCompleted }
}

/** State holder for the Tasks screen: today's tasks plus the add / edit / delete interactions. */
class TasksViewModel(
    private val repository: TaskRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private data class Form(
        val newTitle: String = "",
        val showNewTitleError: Boolean = false,
        val editing: EditingTask? = null,
        val pendingDelete: Task? = null,
        val operationFailed: Boolean = false,
    )

    private val day = MutableStateFlow(time.todayEpochDay())
    private val form = MutableStateFlow(Form())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TasksUiState> = combine(
        day.flatMapLatest { epochDay -> repository.observeTasksForDay(epochDay) },
        form,
    ) { tasks, formState ->
        TasksUiState(
            isLoading = false,
            tasks = tasks,
            newTitle = formState.newTitle,
            showNewTitleError = formState.showNewTitleError,
            editing = formState.editing,
            pendingDelete = formState.pendingDelete,
            operationFailed = formState.operationFailed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TasksUiState(),
    )

    /** Call when the screen becomes visible so the list follows the calendar day. */
    fun refreshDay() {
        day.value = time.todayEpochDay()
    }

    fun onNewTitleChange(text: String) {
        form.update {
            it.copy(newTitle = text.take(TaskTitle.MAX_LENGTH), showNewTitleError = false)
        }
    }

    fun addTask() {
        val title = TaskTitle.sanitize(form.value.newTitle)
        if (title == null) {
            form.update { it.copy(showNewTitleError = true) }
            return
        }
        launchSafely {
            repository.addTask(title = title, epochDay = day.value)
            form.update { it.copy(newTitle = "", showNewTitleError = false) }
        }
    }

    fun toggleCompleted(task: Task) {
        launchSafely { repository.setCompleted(task.id, completed = !task.isCompleted) }
    }

    fun startEditing(task: Task) {
        form.update { it.copy(editing = EditingTask(id = task.id, text = task.title)) }
    }

    fun onEditTextChange(text: String) {
        form.update { current ->
            current.copy(
                editing = current.editing?.copy(
                    text = text.take(TaskTitle.MAX_LENGTH),
                    showError = false,
                ),
            )
        }
    }

    fun confirmEdit() {
        val editing = form.value.editing ?: return
        val title = TaskTitle.sanitize(editing.text)
        if (title == null) {
            form.update { it.copy(editing = editing.copy(showError = true)) }
            return
        }
        launchSafely {
            repository.updateTitle(editing.id, title)
            form.update { it.copy(editing = null) }
        }
    }

    fun cancelEdit() {
        form.update { it.copy(editing = null) }
    }

    fun requestDelete(task: Task) {
        form.update { it.copy(pendingDelete = task) }
    }

    fun confirmDelete() {
        val task = form.value.pendingDelete ?: return
        launchSafely {
            repository.deleteTask(task.id)
            form.update { it.copy(pendingDelete = null) }
        }
    }

    fun cancelDelete() {
        form.update { it.copy(pendingDelete = null) }
    }

    fun dismissError() {
        form.update { it.copy(operationFailed = false) }
    }

    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                // Any storage failure is surfaced as a dismissible message instead of a crash.
                form.update {
                    it.copy(editing = null, pendingDelete = null, operationFailed = true)
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
