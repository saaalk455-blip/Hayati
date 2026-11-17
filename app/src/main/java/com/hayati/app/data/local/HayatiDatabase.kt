package com.hayati.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Local database. Schema export is off for version 1; turn it on (and commit the schema JSON)
 * before the first schema change so migrations can be tested.
 */
@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class HayatiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val NAME = "hayati.db"
    }
}
