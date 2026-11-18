package com.hayati.app.data.adhkar.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One counter per dhikr per category per local day. [progressDate] is `epochDay`. */
@Entity(
    tableName = "adhkar_progress",
    indices = [Index(value = ["progressDate", "category", "dhikrId"], unique = true)],
)
data class AdhkarProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val progressDate: Long,
    val category: String,
    val dhikrId: String,
    val count: Int,
    val updatedAt: Long,
)
