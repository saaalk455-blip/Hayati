package com.hayati.app.data.adhkar

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hayati.app.data.adhkar.repository.AdhkarProgressRepositoryImpl
import com.hayati.app.data.local.HayatiDatabase
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real Room/SQLite on the JVM (Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdhkarProgressDaoTest {
    private lateinit var database: HayatiDatabase
    private lateinit var repository: AdhkarProgressRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HayatiDatabase::class.java).allowMainThreadQueries().build()
        repository = AdhkarProgressRepositoryImpl(database.adhkarProgressDao(), FakeTimeProvider(now = 7_000L))
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `a first count is stored and read back`() = runTest {
        repository.setCount(300, AdhkarCategory.MORNING, "a", 2)
        assertEquals(mapOf("a" to 2), repository.observeCounts(300, AdhkarCategory.MORNING).first())
    }

    @Test
    fun `updating keeps a single row per day category and dhikr`() = runTest {
        repository.setCount(300, AdhkarCategory.MORNING, "a", 1)
        repository.setCount(300, AdhkarCategory.MORNING, "a", 3)
        val rows = database.adhkarProgressDao().observe(300, "morning").first()
        assertEquals(1, rows.size)
        assertEquals(3, rows.single().count)
        assertEquals(7_000L, rows.single().updatedAt)
    }

    @Test
    fun `zero for a dhikr with no row writes nothing but an existing row can go back to zero`() = runTest {
        repository.setCount(300, AdhkarCategory.MORNING, "a", 0)
        assertTrue(database.adhkarProgressDao().observe(300, "morning").first().isEmpty())
        repository.setCount(300, AdhkarCategory.MORNING, "a", 2)
        repository.setCount(300, AdhkarCategory.MORNING, "a", 0)
        assertEquals(mapOf("a" to 0), repository.observeCounts(300, AdhkarCategory.MORNING).first())
    }

    @Test
    fun `days and categories are independent`() = runTest {
        repository.setCount(300, AdhkarCategory.MORNING, "a", 2)
        repository.setCount(301, AdhkarCategory.MORNING, "a", 1)
        repository.setCount(300, AdhkarCategory.EVENING, "a", 3)
        assertEquals(mapOf("a" to 2), repository.observeCounts(300, AdhkarCategory.MORNING).first())
        assertEquals(mapOf("a" to 1), repository.observeCounts(301, AdhkarCategory.MORNING).first())
        assertEquals(mapOf("a" to 3), repository.observeCounts(300, AdhkarCategory.EVENING).first())
        assertTrue(repository.observeCounts(302, AdhkarCategory.MORNING).first().isEmpty())
    }

    @Test
    fun `negative counts are stored as zero`() = runTest {
        repository.setCount(300, AdhkarCategory.MORNING, "a", 1)
        repository.setCount(300, AdhkarCategory.MORNING, "a", -4)
        assertEquals(0, repository.observeCounts(300, AdhkarCategory.MORNING).first().getValue("a"))
    }
}
