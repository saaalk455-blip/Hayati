package com.hayati.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Approved Hayati palette. */
object HayatiPalette {
    val Background = Color(0xFF090F2B)
    val Secondary = Color(0xFF17133D)
    val Primary = Color(0xFF6555D9)
    val Gold = Color(0xFFE6B64A)
    val Card = Color(0xFF202544)
    val Success = Color(0xFF45B987)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA9ADC6)

    /** Subtle card outline: white at low alpha so it works on every dark surface. */
    val Border = Color(0x1FFFFFFF)
}

/**
 * Brand colors that have no Material 3 slot. Exposed through [LocalHayatiColors] so that a light
 * variant can be supplied later without touching any screen.
 */
@Immutable
data class HayatiExtraColors(
    val gold: Color,
    val success: Color,
    val card: Color,
    val textSecondary: Color,
    val border: Color,
)

val DarkExtraColors = HayatiExtraColors(
    gold = HayatiPalette.Gold,
    success = HayatiPalette.Success,
    card = HayatiPalette.Card,
    textSecondary = HayatiPalette.TextSecondary,
    border = HayatiPalette.Border,
)

val LocalHayatiColors = staticCompositionLocalOf { DarkExtraColors }
