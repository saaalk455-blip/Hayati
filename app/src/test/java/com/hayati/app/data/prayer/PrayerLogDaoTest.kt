package com.hayati.app.data.prayer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.local.HayatiDatabase
import com.hayati.app.data.prayer.repository.PrayerLogRepositoryImpl
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.testing.FakeTimeProvider
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

/** Real Room/SQLite on the JVM (Robolectric), covering the prayer tracking repository. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrayerLogDaoTest {
    private lateinit var database: HayatiDatabase
    private lateinit var time: FakeTimeProvider
    private lateinit var repository: PrayerLogRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HayatiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        time = FakeTimeProvider(now = 5_000L)
        repository = PrayerLogRepositoryImpl(database.prayerLogDao(), time as TimeProvider)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `recording a prayer stores it as completed with a timestamp`() = runTest {
        repository.setCompleted(200, Prayer.FAJR, true)
        val logs = repository.observeDay(200).first()
        assertEquals(1, logs.size)
        assertTrue(logs.single().completed)
        assertEquals(5_000L, logs.single().completedAtMillis)
    }

    @Test
    fun `reverting a recorded prayer keeps one row and clears completion`() = runTest {
        repository.setCompleted(200, Prayer.FAJR, true)
        time.now = 9_000L
        repository.setCompleted(200, Prayer.FAJR, false)
        val logs = repository.observeDay(200).first()
        assertEquals(1, logs.size)
        assertFalse(logs.single().completed)
        assertNull(logs.single().completedAtMillis)
    }

    @Test
    fun `reverting a prayer that was never recorded creates nothing`() = runTest {
        repository.setCompleted(200, Prayer.ASR, false)
        assertTrue(repository.observeDay(200).first().isEmpty())
    }

    @Test
    fun `recording twice never creates a duplicate for the same prayer and date`() = runTest {
        repository.setCompleted(200, Prayer.DHUHR, true)
        repository.setCompleted(200, Prayer.DHUHR, true)
        repository.setCompleted(200, Prayer.DHUHR, false)
        repository.setCompleted(200, Prayer.DHUHR, true)
        assertEquals(1, repository.observeDay(200).first().size)
    }

    @Test
    fun `the same prayer on different dates and different prayers on one date are separate`() = runTest {
        repository.setCompleted(200, Prayer.FAJR, true)
        repository.setCompleted(201, Prayer.FAJR, true)
        repository.setCompleted(200, Prayer.ISHA, true)
        assertEquals(2, repository.observeDay(200).first().size)
        assertEquals(1, repository.observeDay(201).first().size)
    }

    @Test
    fun `history range returns only stored records within the range`() = runTest {
        repository.setCompleted(190, Prayer.FAJR, true)
        repository.setCompleted(195, Prayer.DHUHR, true)
        repository.setCompleted(200, Prayer.ASR, true)
        val range = repository.observeRange(191, 200).first()
        assertEquals(listOf(200L, 195L), range.map { it.epochDay })
        assertTrue(repository.observeRange(10, 20).first().isEmpty())
    }

    @Test
    fun `unknown prayer types in storage are ignored instead of crashing`() = runTest {
        database.prayerLogDao().insert(
            com.hayati.app.data.prayer.local.PrayerLogEntity(
                prayerDate = 300, prayerType = "SUNRISE", status = "COMPLETED",
                completedAt = 1, createdAt = 1, updatedAt = 1,
            ),
        )
        assertTrue(repository.observeDay(300).first().isEmpty())
    }
}
