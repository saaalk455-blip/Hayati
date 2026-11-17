package com.hayati.app.data.repository

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.local.TaskDao
import com.hayati.app.data.local.TaskEntity
import com.hayati.app.data.local.toDomain
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.model.TaskTitle
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val time: TimeProvider,
) : TaskRepository {

    override fun observeTasksForDay(epochDay: Long): Flow<List<Task>> =
        dao.observeByDay(epochDay).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTask(title: String, epochDay: Long) {
        dao.insert(
            TaskEntity(
                title = requireTitle(title),
                isCompleted = false,
                epochDay = epochDay,
                createdAtMillis = time.nowMillis(),
                completedAtMillis = null,
            ),
        )
    }

    override suspend fun updateTitle(id: Long, title: String) {
        dao.updateTitle(id, requireTitle(title))
    }

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        dao.setCompleted(
            id = id,
            completed = completed,
            completedAtMillis = if (completed) time.nowMillis() else null,
        )
    }

    override suspend fun deleteTask(id: Long) {
        dao.deleteById(id)
    }

    private fun requireTitle(raw: String): String =
        TaskTitle.sanitize(raw) ?: throw IllegalArgumentException("Task title must not be blank")
}
