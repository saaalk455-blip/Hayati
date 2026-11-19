package com.hayati.app.data.adhkar

import com.hayati.app.data.adhkar.content.AdhkarJsonParser
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.repository.AdhkarContentException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** org.json needs the Android runtime, hence Robolectric. All texts here are test-only. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdhkarJsonParserTest {
    private fun file(vararg items: String) = """{"version":1,"source":"test","adhkar":[${items.joinToString(",")}]}"""
    private fun item(id: String = "t1", cat: String = "morning", text: String = "نص اختبار", repeat: String = "\"repeat\":3,") =
        """{"id":"$id","category":"$cat","text":"$text",$repeat"source":"مرجع اختبار"}"""

    @Test
    fun `parses a valid entry without changing its text`() {
        val result = AdhkarJsonParser.parse(file(item(text = "  نص اختبار  ")))
        val dhikr = result.single()
        assertEquals("t1", dhikr.id)
        assertEquals(AdhkarCategory.MORNING, dhikr.category)
        assertEquals("نص اختبار", dhikr.text)
        assertEquals(3, dhikr.repeat)
        assertEquals("مرجع اختبار", dhikr.source)
    }

    @Test
    fun `a missing repeat is rejected, never assumed to be one`() {
        assertThrows(AdhkarContentException::class.java) { AdhkarJsonParser.parse(file(item(repeat = ""))) }
    }

    @Test
    fun `takhrij is read when present and empty when absent`() {
        val withTakhrij = """{"id":"t1","category":"evening","text":"نص اختبار","repeat":1,"source":"م","takhrij":"تخريج اختبار"}"""
        assertEquals("تخريج اختبار", AdhkarJsonParser.parse(file(withTakhrij)).single().takhrij)
        assertEquals("", AdhkarJsonParser.parse(file(item())).single().takhrij)
    }

    @Test
    fun `the shipped empty file parses to no adhkar`() {
        assertTrue(AdhkarJsonParser.parse("""{"version":0,"source":"","adhkar":[]}""").isEmpty())
    }

    @Test
    fun `invalid files are rejected instead of partly loaded`() {
        listOf(
            file(item(), item()),
            file(item(cat = "night")),
            file(item(text = "   ")),
            file(item(repeat = "\"repeat\":0,")),
            "not json",
            """{"version":1}""",
        ).forEach { bad ->
            assertThrows(bad, AdhkarContentException::class.java) { AdhkarJsonParser.parse(bad) }
        }
    }
}
