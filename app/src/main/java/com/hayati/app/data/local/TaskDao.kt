package com.hayati.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks WHERE epochDay = :epochDay " +
            "ORDER BY isCompleted ASC, createdAtMillis ASC, id ASC",
    )
    fun observeByDay(epochDay: Long): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query(
        "UPDATE tasks SET isCompleted = :completed, completedAtMillis = :completedAtMillis " +
            "WHERE id = :id",
    )
    suspend fun setCompleted(id: Long, completed: Boolean, completedAtMillis: Long?)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
