package com.hayati.app.data.repository

import com.hayati.app.testing.FakeTaskDao
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TaskRepositoryImplTest {

    private val dao = FakeTaskDao()
    private val time = FakeTimeProvider(today = 50L, now = 5_000L)
    private val repository = TaskRepositoryImpl(dao, time)

    @Test
    fun addTask_stores_a_trimmed_open_task_stamped_with_the_current_time() = runTest {
        repository.addTask("  اقرأ  ", epochDay = 50L)

        val task = repository.observeTasksForDay(50L).first().single()
        assertEquals("اقرأ", task.title)
        assertFalse(task.isCompleted)
        assertEquals(50L, task.epochDay)
        assertEquals(5_000L, task.createdAtMillis)
        assertNull(task.completedAtMillis)
    }

    @Test
    fun addTask_rejects_a_blank_title_and_stores_nothing() = runTest {
        try {
            repository.addTask("   ", epochDay = 50L)
            fail("Expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // expected
        }

        assertTrue(dao.all.isEmpty())
    }

    @Test
    fun observeTasksForDay_only_returns_that_day() = runTest {
        repository.addTask("اليوم", epochDay = 50L)
        repository.addTask("غدًا", epochDay = 51L)

        val titles = repository.observeTasksForDay(50L).first().map { it.title }

        assertEquals(listOf("اليوم"), titles)
    }

    @Test
    fun setCompleted_records_and_clears_the_completion_time() = runTest {
        repository.addTask("مهمة", epochDay = 50L)
        val id = dao.all.single().id

        time.now = 9_000L
        repository.setCompleted(id, completed = true)
        val done = repository.observeTasksForDay(50L).first().single()
        assertTrue(done.isCompleted)
        assertEquals(9_000L, done.completedAtMillis)

        repository.setCompleted(id, completed = false)
        val reopened = repository.observeTasksForDay(50L).first().single()
        assertFalse(reopened.isCompleted)
        assertNull(reopened.completedAtMillis)
    }

    @Test
    fun updateTitle_changes_the_title_and_rejects_blank() = runTest {
        repository.addTask("قديم", epochDay = 50L)
        val id = dao.all.single().id

        repository.updateTitle(id, "  جديد ")
        assertEquals("جديد", repository.observeTasksForDay(50L).first().single().title)

        try {
            repository.updateTitle(id, " ")
            fail("Expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // expected
        }
        assertEquals("جديد", repository.observeTasksForDay(50L).first().single().title)
    }

    @Test
    fun deleteTask_removes_only_that_task() = runTest {
        repository.addTask("أ", epochDay = 50L)
        repository.addTask("ب", epochDay = 50L)
        val firstId = dao.all.first().id

        repository.deleteTask(firstId)

        assertEquals(listOf("ب"), repository.observeTasksForDay(50L).first().map { it.title })
    }

    @Test
    fun open_tasks_are_listed_before_completed_ones() = runTest {
        repository.addTask("الأولى", epochDay = 50L)
        time.now = 6_000L
        repository.addTask("الثانية", epochDay = 50L)
        repository.setCompleted(dao.all.first().id, completed = true)

        val titles = repository.observeTasksForDay(50L).first().map { it.title }

        assertEquals(listOf("الثانية", "الأولى"), titles)
    }
}
