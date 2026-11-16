package com.hayati.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val HayatiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Spacing scale shared by every screen. */
object HayatiSpacing {
    val ScreenHorizontal = 20.dp
    val ScreenVertical = 16.dp
    val Section = 20.dp
    val Item = 12.dp
    val CardPadding = 16.dp
}
