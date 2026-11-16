package com.hayati.app.core.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Small set of original 24x24 icons for concepts the core Material icon set does not cover.
 * Built from simple path data so no extra icon dependency is needed.
 */
object HayatiIcons {

    val Worship: ImageVector by lazy {
        icon(
            "Worship",
            // Crescent.
            "M12,3 A9,9 0 1 0 21,12 A7,7 0 0 1 12,3 Z",
        )
    }

    val Quran: ImageVector by lazy {
        icon(
            "Quran",
            // Open book: two pages.
            "M4,5 H11 A1,1 0 0 1 12,6 V19 A2,2 0 0 0 10,17 H4 Z " +
                "M20,5 H13 A1,1 0 0 0 12,6 V19 A2,2 0 0 1 14,17 H20 Z",
        )
    }

    val Adhkar: ImageVector by lazy {
        icon(
            "Adhkar",
            // Four-point sparkle.
            "M12,3 L14.2,9.8 L21,12 L14.2,14.2 L12,21 L9.8,14.2 L3,12 L9.8,9.8 Z",
        )
    }

    val Tasbeeh: ImageVector by lazy {
        icon(
            "Tasbeeh",
            // Three beads.
            "M12,3 A2.5,2.5 0 1 0 12,8 A2.5,2.5 0 1 0 12,3 Z " +
                "M6,13 A2.5,2.5 0 1 0 6,18 A2.5,2.5 0 1 0 6,13 Z " +
                "M18,13 A2.5,2.5 0 1 0 18,18 A2.5,2.5 0 1 0 18,13 Z",
        )
    }

    val Statistics: ImageVector by lazy {
        icon(
            "Statistics",
            // Three bars.
            "M5,11 H9 V20 H5 Z M10,4 H14 V20 H10 Z M15,8 H19 V20 H15 Z",
        )
    }

    /** All custom icons, used by tests to make sure every path parses. */
    val all: List<ImageVector>
        get() = listOf(Worship, Quran, Adhkar, Tasbeeh, Statistics)

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = addPathNodes(pathData),
            fill = SolidColor(Color.Black),
        ).build()
}
