package com.hayati.app.domain.repository

import com.hayati.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    /** Emits the tasks of [epochDay] (open tasks first, then completed) and re-emits on every change. */
    fun observeTasksForDay(epochDay: Long): Flow<List<Task>>

    /** @throws IllegalArgumentException if [title] is blank. */
    suspend fun addTask(title: String, epochDay: Long)

    /** @throws IllegalArgumentException if [title] is blank. */
    suspend fun updateTitle(id: Long, title: String)

    suspend fun setCompleted(id: Long, completed: Boolean)

    suspend fun deleteTask(id: Long)
}
