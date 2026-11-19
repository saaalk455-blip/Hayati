package com.hayati.app.data.adhkar

import com.hayati.app.data.adhkar.content.AdhkarJsonParser
import com.hayati.app.data.adhkar.content.AdhkarReleaseRules
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Checks the file that really ships in the app (`src/main/assets/adhkar.json`), not a fixture.
 * Unit tests run with the module directory as working directory.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdhkarShippedContentTest {
    private val file = File("src/main/assets/adhkar.json")

    @Test
    fun `the shipped file exists, parses strictly and satisfies the release rules`() {
        assertTrue("assets/adhkar.json is missing at ${file.absolutePath}", file.isFile)
        val adhkar = AdhkarJsonParser.parse(file.readText(Charsets.UTF_8))
        val problems = AdhkarReleaseRules.violations(adhkar)
        assertTrue("Release rule violations: $problems", problems.isEmpty())
    }

    /**
     * Release gate. It is *skipped* (not passed) while the shipped file has no adhkar, so the report
     * never claims real content exists when it does not. It runs automatically once content is added.
     */
    @Test
    fun `release gate - every category has real adhkar`() {
        val adhkar = AdhkarJsonParser.parse(file.readText(Charsets.UTF_8))
        assumeTrue("No adhkar content shipped yet (KI-025)", adhkar.isNotEmpty())
        com.hayati.app.domain.adhkar.model.AdhkarCategory.entries.forEach { category ->
            assertTrue("No adhkar for $category", adhkar.any { it.category == category })
        }
    }

    @Test
    fun `shipped counts match the audited book chapters`() {
        val adhkar = AdhkarJsonParser.parse(file.readText(Charsets.UTF_8))
        val counts = adhkar.groupingBy { it.category }.eachCount()
        // Chapter 25 = 8 entries; chapter 27 = 24 entries, 3 morning-only and 1 evening-only.
        org.junit.Assert.assertEquals(8, counts[com.hayati.app.domain.adhkar.model.AdhkarCategory.AFTER_PRAYER])
        org.junit.Assert.assertEquals(23, counts[com.hayati.app.domain.adhkar.model.AdhkarCategory.MORNING])
        org.junit.Assert.assertEquals(21, counts[com.hayati.app.domain.adhkar.model.AdhkarCategory.EVENING])
    }

    @Test
    fun `shipped entries have a source, no test text, no leftover footnote or verse numbers`() {
        val adhkar = AdhkarJsonParser.parse(file.readText(Charsets.UTF_8))
        adhkar.forEach {
            assertTrue("${it.id}: source must cite the book", it.source.contains("حصن المسلم"))
            assertTrue("${it.id}: takhrij missing", it.takhrij.isNotBlank())
            assertTrue("${it.id}: stray footnote marker", !Regex("\\(\\d+\\)").containsMatchIn(it.text))
        }
        assertTrue(adhkar.any { it.repeat == 33 || it.repeat == 100 || it.repeat == 10 || it.repeat == 7 })
    }
}
