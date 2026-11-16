package com.hayati.app.core.designsystem.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HayatiIconsTest {

    @Test
    fun every_custom_icon_path_parses_and_is_not_empty() {
        HayatiIcons.all.forEach { icon ->
            assertTrue("${icon.name} has no paths", icon.root.size > 0)
        }
    }

    @Test
    fun icons_use_a_24_unit_viewport() {
        HayatiIcons.all.forEach { icon ->
            assertEquals(24f, icon.viewportWidth, 0f)
            assertEquals(24f, icon.viewportHeight, 0f)
        }
    }
}
