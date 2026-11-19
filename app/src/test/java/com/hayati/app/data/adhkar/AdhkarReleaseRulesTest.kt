package com.hayati.app.data.adhkar

import com.hayati.app.data.adhkar.content.AdhkarReleaseRules
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhkarReleaseRulesTest {
    private fun d(
        id: String = "a",
        text: String = "نص حقيقي مفترض",
        source: String = "مصدر",
        takhrij: String = "تخريج",
        cat: AdhkarCategory = AdhkarCategory.MORNING,
    ) = Dhikr(id, cat, text, 1, source, takhrij)

    @Test
    fun `a fully documented list has no violations`() {
        assertTrue(AdhkarReleaseRules.violations(listOf(d("a"), d("b", text = "نص آخر"))).isEmpty())
        assertTrue(AdhkarReleaseRules.violations(emptyList()).isEmpty())
    }

    @Test
    fun `missing source or takhrij is reported`() {
        val problems = AdhkarReleaseRules.violations(listOf(d(source = ""), d("b", text = "آخر", takhrij = " ")))
        assertEquals(listOf("a: missing source", "b: missing takhrij"), problems)
    }

    @Test
    fun `placeholder text is reported`() {
        assertEquals(1, AdhkarReleaseRules.violations(listOf(d(text = "ذكر تجريبي"))).size)
        assertEquals(1, AdhkarReleaseRules.violations(listOf(d(text = "نص اختبار"))).size)
    }

    @Test
    fun `the same text twice in one category is reported but across categories is allowed`() {
        assertEquals(1, AdhkarReleaseRules.violations(listOf(d("a"), d("b"))).size)
        assertTrue(AdhkarReleaseRules.violations(listOf(d("a"), d("b", cat = AdhkarCategory.EVENING))).isEmpty())
    }
}
