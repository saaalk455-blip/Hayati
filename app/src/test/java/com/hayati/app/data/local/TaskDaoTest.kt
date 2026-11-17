package com.hayati.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Runs the real Room database (SQLite) on the JVM through Robolectric. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskDaoTest {

    private lateinit var context: Context
    private lateinit var database: HayatiDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, HayatiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(
        title: String,
        epochDay: Long = 10L,
        completed: Boolean = false,
        createdAt: Long = 1L,
    ) = TaskEntity(
        title = title,
        isCompleted = completed,
        epochDay = epochDay,
        createdAtMillis = createdAt,
        completedAtMillis = if (completed) createdAt else null,
    )

    @Test
    fun insert_assigns_increasing_ids_and_the_task_is_observable() = runTest {
        val first = dao.insert(entity("أ"))
        val second = dao.insert(entity("ب"))

        assertTrue(second > first)
        assertEquals(listOf("أ", "ب"), dao.observeByDay(10L).first().map { it.title })
    }

    @Test
    fun observeByDay_filters_by_day() = runTest {
        dao.insert(entity("اليوم", epochDay = 10L))
        dao.insert(entity("غدًا", epochDay = 11L))

        assertEquals(listOf("اليوم"), dao.observeByDay(10L).first().map { it.title })
        assertEquals(listOf("غدًا"), dao.observeByDay(11L).first().map { it.title })
        assertTrue(dao.observeByDay(12L).first().isEmpty())
    }

    @Test
    fun open_tasks_come_before_completed_then_oldest_first() = runTest {
        dao.insert(entity("منجزة", completed = true, createdAt = 1L))
        dao.insert(entity("مفتوحة متأخرة", createdAt = 3L))
        dao.insert(entity("مفتوحة مبكرة", createdAt = 2L))

        assertEquals(
            listOf("مفتوحة مبكرة", "مفتوحة متأخرة", "منجزة"),
            dao.observeByDay(10L).first().map { it.title },
        )
    }

    @Test
    fun updateTitle_changes_only_the_title() = runTest {
        val id = dao.insert(entity("قديم"))

        dao.updateTitle(id, "جديد")

        val task = dao.observeByDay(10L).first().single()
        assertEquals("جديد", task.title)
        assertFalse(task.isCompleted)
    }

    @Test
    fun setCompleted_stores_and_clears_the_completion_time() = runTest {
        val id = dao.insert(entity("مهمة"))

        dao.setCompleted(id, completed = true, completedAtMillis = 777L)
        val done = dao.observeByDay(10L).first().single()
        assertTrue(done.isCompleted)
        assertEquals(777L, done.completedAtMillis)

        dao.setCompleted(id, completed = false, completedAtMillis = null)
        val reopened = dao.observeByDay(10L).first().single()
        assertFalse(reopened.isCompleted)
        assertNull(reopened.completedAtMillis)
    }

    @Test
    fun deleteById_removes_the_row() = runTest {
        val keep = dao.insert(entity("ابقَ"))
        val remove = dao.insert(entity("احذف"))

        dao.deleteById(remove)

        assertEquals(listOf(keep), dao.observeByDay(10L).first().map { it.id })
    }

    @Test
    fun tasks_are_still_there_after_the_database_is_closed_and_reopened() = runTest {
        val name = "persistence-test.db"
        val first = Room.databaseBuilder(context, HayatiDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        first.taskDao().insert(entity("باقية بعد الإغلاق"))
        first.close()

        val reopened = Room.databaseBuilder(context, HayatiDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        try {
            val titles = reopened.taskDao().observeByDay(10L).first().map { it.title }
            assertEquals(listOf("باقية بعد الإغلاق"), titles)
        } finally {
            reopened.close()
            context.deleteDatabase(name)
        }
    }
}
