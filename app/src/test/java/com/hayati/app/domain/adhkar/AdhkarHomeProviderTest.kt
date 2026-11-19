package com.hayati.app.domain.adhkar

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeProvider
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeState
import com.hayati.app.testing.FakeAdhkarContentSource
import com.hayati.app.testing.FakeAdhkarProgressRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class AdhkarHomeProviderTest {
    private val m1 = Dhikr("m1", AdhkarCategory.MORNING, "نص ١", 2, "")
    private val m2 = Dhikr("m2", AdhkarCategory.MORNING, "نص ٢", 1, "")
    private val e1 = Dhikr("e1", AdhkarCategory.EVENING, "نص ٣", 1, "")
    private val utc = ZoneId.of("UTC")
    private fun at(hour: Int) = Instant.parse("2026-06-15T%02d:00:00Z".format(hour)).toEpochMilli()

    private suspend fun state(
        content: FakeAdhkarContentSource,
        progress: FakeAdhkarProgressRepository = FakeAdhkarProgressRepository(),
        hour: Int = 8,
        day: Long = 10,
    ) = AdhkarHomeProvider(content, progress).observe(flowOf(day), flowOf(at(hour)), utc).first()

    @Test
    fun `morning hours show the morning category with zero progress`() = runTest {
        val s = state(FakeAdhkarContentSource(listOf(m1, m2, e1)))
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.MORNING, 0, 2), s)
    }

    @Test
    fun `afternoon hours show the evening category`() = runTest {
        val s = state(FakeAdhkarContentSource(listOf(m1, m2, e1)), hour = 15)
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.EVENING, 0, 1), s)
    }

    @Test
    fun `progress is read from the same counters the screen writes, for the given local day only`() = runTest {
        val progress = FakeAdhkarProgressRepository()
        progress.setCount(10, AdhkarCategory.MORNING, "m1", 2)
        progress.setCount(10, AdhkarCategory.MORNING, "m2", 0)
        progress.setCount(9, AdhkarCategory.MORNING, "m2", 1)
        val content = FakeAdhkarContentSource(listOf(m1, m2, e1))
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.MORNING, 1, 2), state(content, progress, day = 10))
        // Day 9: only m2 is done (its repeat is 1), m1 is not.
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.MORNING, 1, 2), state(content, progress, day = 9))
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.MORNING, 0, 2), state(content, progress, day = 11))
    }

    @Test
    fun `a counter above the repeat still counts the dhikr once`() = runTest {
        val progress = FakeAdhkarProgressRepository()
        progress.setCount(10, AdhkarCategory.MORNING, "m1", 99)
        val s = state(FakeAdhkarContentSource(listOf(m1, m2)), progress) as AdhkarHomeState.Ready
        assertEquals(1, s.doneCount)
    }

    @Test
    fun `completion is reported when every dhikr is done`() = runTest {
        val progress = FakeAdhkarProgressRepository()
        progress.setCount(10, AdhkarCategory.EVENING, "e1", 1)
        val s = state(FakeAdhkarContentSource(listOf(e1)), progress, hour = 20) as AdhkarHomeState.Ready
        assertEquals(true, s.isComplete)
    }

    @Test
    fun `falls back to the other category when the suggested one has no adhkar`() = runTest {
        val s = state(FakeAdhkarContentSource(listOf(e1)), hour = 8)
        assertEquals(AdhkarHomeState.Ready(AdhkarCategory.EVENING, 0, 1), s)
    }

    @Test
    fun `no content or an unreadable file is unavailable, never invented`() = runTest {
        assertEquals(AdhkarHomeState.Unavailable, state(FakeAdhkarContentSource(emptyList())))
        assertEquals(AdhkarHomeState.Unavailable, state(FakeAdhkarContentSource(failure = IllegalStateException("x"))))
    }

    @Test
    fun `the suggested category follows the local time zone`() {
        val instant = Instant.parse("2026-06-15T22:00:00Z").toEpochMilli()
        assertEquals(AdhkarCategory.EVENING, AdhkarHomeProvider.suggestedCategory(instant, ZoneId.of("UTC")))
        // 22:00 UTC is 01:00 next day in Cairo (UTC+3 in June): morning side of the clock.
        assertEquals(AdhkarCategory.MORNING, AdhkarHomeProvider.suggestedCategory(instant, ZoneId.of("Africa/Cairo")))
    }
}
