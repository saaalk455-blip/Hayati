package com.hayati.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.hayati.app.core.designsystem.theme.LocalHayatiColors

/** Restrained decorative crescent with a few faint stars, drawn with Canvas (no image assets). */
@Composable
fun CrescentDecoration(
    modifier: Modifier = Modifier,
    color: Color = LocalHayatiColors.current.gold,
) {
    Canvas(modifier = modifier) {
        drawCrescent(color)
    }
}

private fun DrawScope.drawCrescent(color: Color) {
    val radius = size.minDimension * 0.34f
    val c = center

    val outer = Path().apply { addOval(Rect(center = c, radius = radius)) }
    val inner = Path().apply {
        addOval(
            Rect(
                center = Offset(c.x + radius * 0.42f, c.y - radius * 0.20f),
                radius = radius * 0.84f,
            ),
        )
    }
    val crescent = Path.combine(PathOperation.Difference, outer, inner)
    drawPath(path = crescent, color = color.copy(alpha = 0.92f))

    val starRadius = size.minDimension * 0.022f
    val stars = listOf(
        Offset(c.x + radius * 0.80f, c.y - radius * 0.78f) to 1.0f,
        Offset(c.x + radius * 1.12f, c.y - radius * 0.20f) to 0.7f,
        Offset(c.x + radius * 0.62f, c.y + radius * 0.52f) to 0.5f,
    )
    stars.forEach { (offset, scale) ->
        drawCircle(
            color = color.copy(alpha = 0.7f * scale),
            radius = starRadius * (0.6f + scale * 0.6f),
            center = offset,
        )
    }
}
