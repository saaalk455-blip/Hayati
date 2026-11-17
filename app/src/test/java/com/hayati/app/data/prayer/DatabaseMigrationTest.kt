package com.hayati.app.data.prayer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hayati.app.data.local.HayatiDatabase
import com.hayati.app.domain.prayer.model.Prayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Builds a genuine version-1 database file (the Phase 3 schema, with a task in it), then opens it
 * with the current database and the real migration, and checks the task survived and the new
 * prayer table works.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {
    @Test
    fun `migrating from version 1 keeps tasks and adds prayer logs`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = context.getDatabasePath("migration-test.db")
        file.parentFile?.mkdirs()
        file.delete()

        SQLiteDatabase.openOrCreateDatabase(file, null).use { v1 ->
            v1.execSQL(
                "CREATE TABLE `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, " +
                    "`createdAtMillis` INTEGER NOT NULL, `completedAtMillis` INTEGER)",
            )
            v1.execSQL("CREATE INDEX `index_tasks_epochDay` ON `tasks` (`epochDay`)")
            v1.execSQL("INSERT INTO tasks (title, isCompleted, epochDay, createdAtMillis) VALUES ('قديمة', 0, 100, 1)")
            v1.version = 1
        }

        val database = Room.databaseBuilder(context, HayatiDatabase::class.java, "migration-test.db")
            .addMigrations(HayatiDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        try {
            val tasks = database.taskDao().observeByDay(100).first()
            assertEquals(listOf("قديمة"), tasks.map { it.title })

            database.prayerLogDao().setStatus(100, Prayer.FAJR.name, true, 1L)
            assertEquals(1, database.prayerLogDao().observeDay(100).first().size)
        } finally {
            database.close()
        }
    }
}
