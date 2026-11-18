package com.hayati.app.domain.adhkar

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.AdhkarCounter
import com.hayati.app.domain.adhkar.model.CategoryProgress
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.model.DhikrProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhkarCounterTest {
    private fun dhikr(id: String, repeat: Int) = Dhikr(id, AdhkarCategory.MORNING, "نص اختبار $id", repeat, "")

    @Test
    fun `increment stops at the required repeat`() {
        assertEquals(1, AdhkarCounter.increment(0, 3))
        assertEquals(3, AdhkarCounter.increment(2, 3))
        assertEquals(3, AdhkarCounter.increment(3, 3))
    }

    @Test
    fun `decrement never goes below zero`() {
        assertEquals(1, AdhkarCounter.decrement(2, 3))
        assertEquals(0, AdhkarCounter.decrement(0, 3))
    }

    @Test
    fun `stored counts above the repeat are clamped and negatives become zero`() {
        assertEquals(3, AdhkarCounter.clamp(99, 3))
        assertEquals(0, AdhkarCounter.clamp(-5, 3))
    }

    @Test
    fun `a repeat below one is treated as one`() {
        assertEquals(1, AdhkarCounter.increment(0, 0))
    }

    @Test
    fun `category progress counts only finished adhkar and is complete when all are done`() {
        val items = listOf(
            DhikrProgress(dhikr("a", 2), 2),
            DhikrProgress(dhikr("b", 3), 1),
        )
        val partial = CategoryProgress(AdhkarCategory.MORNING, items)
        assertEquals(1, partial.doneCount)
        assertFalse(partial.isComplete)
        assertEquals(2, items[1].remaining)

        val full = CategoryProgress(AdhkarCategory.MORNING, listOf(DhikrProgress(dhikr("a", 2), 2)))
        assertTrue(full.isComplete)
    }

    @Test
    fun `an empty category is never complete`() {
        assertFalse(CategoryProgress(AdhkarCategory.EVENING, emptyList()).isComplete)
        assertNull(AdhkarCategory.fromKey("unknown"))
        assertEquals(AdhkarCategory.AFTER_PRAYER, AdhkarCategory.fromKey("after_prayer"))
    }
}
