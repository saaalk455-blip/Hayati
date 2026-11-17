package com.hayati.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hayati.app.domain.model.Task

@Entity(tableName = "tasks", indices = [Index(value = ["epochDay"])])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean,
    val epochDay: Long,
    val createdAtMillis: Long,
    val completedAtMillis: Long?,
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    isCompleted = isCompleted,
    epochDay = epochDay,
    createdAtMillis = createdAtMillis,
    completedAtMillis = completedAtMillis,
)
