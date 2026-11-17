package com.hayati.app.testing

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.local.TaskDao
import com.hayati.app.data.local.TaskEntity
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.model.TaskTitle
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTimeProvider(
    var today: Long = 100L,
    var now: Long = 1_000L,
) : TimeProvider {
    override fun todayEpochDay(): Long = today

    override fun nowMillis(): Long = now
}

/** In-memory [TaskRepository]; set [failure] to make every write throw. */
class FakeTaskRepository(
    private val time: TimeProvider = FakeTimeProvider(),
) : TaskRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    var failure: Exception? = null

    val allTasks: List<Task> get() = tasks.value

    fun seed(
        title: String,
        epochDay: Long,
        completed: Boolean = false,
    ): Task {
        val task = Task(
            id = nextId++,
            title = title,
            isCompleted = completed,
            epochDay = epochDay,
            createdAtMillis = time.nowMillis(),
            completedAtMillis = if (completed) time.nowMillis() else null,
        )
        tasks.update { it + task }
        return task
    }

    override fun observeTasksForDay(epochDay: Long): Flow<List<Task>> =
        tasks.map { all -> all.filter { it.epochDay == epochDay } }

    override suspend fun addTask(title: String, epochDay: Long) {
        failIfRequested()
        val clean = TaskTitle.sanitize(title) ?: throw IllegalArgumentException("blank title")
        seed(clean, epochDay)
    }

    override suspend fun updateTitle(id: Long, title: String) {
        failIfRequested()
        val clean = TaskTitle.sanitize(title) ?: throw IllegalArgumentException("blank title")
        tasks.update { all -> all.map { if (it.id == id) it.copy(title = clean) else it } }
    }

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        failIfRequested()
        tasks.update { all ->
            all.map {
                if (it.id == id) {
                    it.copy(
                        isCompleted = completed,
                        completedAtMillis = if (completed) time.nowMillis() else null,
                    )
                } else {
                    it
                }
            }
        }
    }

    override suspend fun deleteTask(id: Long) {
        failIfRequested()
        tasks.update { all -> all.filterNot { it.id == id } }
    }

    private fun failIfRequested() {
        failure?.let { throw it }
    }
}

/** In-memory [TaskDao] that mimics the real query ordering. */
class FakeTaskDao : TaskDao {
    private val rows = MutableStateFlow<List<TaskEntity>>(emptyList())
    private var nextId = 1L

    val all: List<TaskEntity> get() = rows.value

    override fun observeByDay(epochDay: Long): Flow<List<TaskEntity>> =
        rows.map { list ->
            list.filter { it.epochDay == epochDay }
                .sortedWith(compareBy({ it.isCompleted }, { it.createdAtMillis }, { it.id }))
        }

    override suspend fun insert(task: TaskEntity): Long {
        val saved = task.copy(id = nextId++)
        rows.update { it + saved }
        return saved.id
    }

    override suspend fun updateTitle(id: Long, title: String) {
        rows.update { list -> list.map { if (it.id == id) it.copy(title = title) else it } }
    }

    override suspend fun setCompleted(id: Long, completed: Boolean, completedAtMillis: Long?) {
        rows.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(isCompleted = completed, completedAtMillis = completedAtMillis)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun deleteById(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }
}
