package com.hayati.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val HayatiDarkColorScheme = darkColorScheme(
    primary = HayatiPalette.Primary,
    onPrimary = HayatiPalette.TextPrimary,
    primaryContainer = HayatiPalette.Primary,
    onPrimaryContainer = HayatiPalette.TextPrimary,
    secondary = HayatiPalette.Gold,
    onSecondary = HayatiPalette.Background,
    secondaryContainer = HayatiPalette.Secondary,
    onSecondaryContainer = HayatiPalette.TextPrimary,
    tertiary = HayatiPalette.Success,
    onTertiary = HayatiPalette.Background,
    background = HayatiPalette.Background,
    onBackground = HayatiPalette.TextPrimary,
    surface = HayatiPalette.Secondary,
    onSurface = HayatiPalette.TextPrimary,
    surfaceVariant = HayatiPalette.Card,
    onSurfaceVariant = HayatiPalette.TextSecondary,
    surfaceContainerLowest = HayatiPalette.Background,
    surfaceContainerLow = HayatiPalette.Background,
    surfaceContainer = HayatiPalette.Secondary,
    surfaceContainerHigh = HayatiPalette.Card,
    surfaceContainerHighest = HayatiPalette.Card,
    outline = HayatiPalette.TextSecondary,
    outlineVariant = HayatiPalette.Border,
)

/**
 * Hayati theme.
 *
 * Only the premium dark scheme exists today. To add light mode later, introduce a light
 * `ColorScheme` and `HayatiExtraColors` and select them here; screens only read colors from
 * [MaterialTheme] and [LocalHayatiColors], so they will not need to change.
 *
 * The layout direction is forced to RTL because the interface is Arabic-only for now, even when
 * the device language is not Arabic. Revisit this when a language setting is introduced.
 */
@Composable
fun HayatiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalHayatiColors provides DarkExtraColors,
    ) {
        MaterialTheme(
            colorScheme = HayatiDarkColorScheme,
            typography = HayatiTypography,
            shapes = HayatiShapes,
            content = content,
        )
    }
}
