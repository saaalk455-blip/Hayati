package com.hayati.app.navigation

import com.hayati.app.feature.worship.WorshipSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HayatiRoutesTest {

    @Test
    fun bottom_bar_has_the_four_required_destinations_in_order() {
        assertEquals(
            listOf("home", "worship", "tasks", "statistics"),
            HayatiRoutes.topLevel,
        )
    }

    @Test
    fun top_level_destinations_match_the_route_list() {
        assertEquals(HayatiRoutes.topLevel, TopLevelDestination.entries.map { it.route })
    }

    @Test
    fun every_route_in_the_graph_is_unique() {
        val all = HayatiRoutes.topLevel +
            HayatiRoutes.SETTINGS +
            HayatiRoutes.PRAYER_SETTINGS +
            HayatiRoutes.PRAYER_HISTORY +
            WorshipSection.entries.map { it.route }

        assertEquals(all.size, all.toSet().size)
    }

    @Test
    fun adhkar_route_builder_matches_the_graph_pattern() {
        assertEquals("section/adhkar", HayatiRoutes.adhkar())
        assertEquals("section/adhkar?category=evening", HayatiRoutes.adhkar("evening"))
        assertEquals(WorshipSection.Adhkar.route, HayatiRoutes.adhkar())
        assertEquals("section/adhkar?category={category}", HayatiRoutes.ADHKAR_PATTERN)
    }

    @Test
    fun settings_is_not_a_bottom_bar_destination() {
        assertFalse(HayatiRoutes.SETTINGS in HayatiRoutes.topLevel)
    }
}
