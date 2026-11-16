package com.hayati.app.feature.worship

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorshipSectionTest {

    @Test
    fun routes_are_unique() {
        val routes = WorshipSection.entries.map { it.route }

        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun routes_are_namespaced_under_section() {
        assertTrue(WorshipSection.entries.all { it.route.startsWith("section/") })
    }

    @Test
    fun has_the_four_required_sections() {
        assertEquals(
            listOf("Prayer", "Quran", "Adhkar", "Tasbeeh"),
            WorshipSection.entries.map { it.name },
        )
    }
}
