package com.hayati.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Arabic text must keep letterSpacing at zero: any positive spacing breaks the joining of
 * Arabic letters. Line heights are slightly generous because Arabic diacritics and ascenders
 * need more vertical room than Latin text.
 *
 * The system font family is used on purpose: Android ships an Arabic-capable face (Noto family)
 * with correct shaping, so no font asset needs to be bundled or licensed.
 */
private val NoSpacing: TextUnit = 0.sp

private fun arabicStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = NoSpacing,
)

val HayatiTypography = Typography(
    displaySmall = arabicStyle(36, 48, FontWeight.Bold),
    headlineMedium = arabicStyle(28, 40, FontWeight.Bold),
    headlineSmall = arabicStyle(24, 34, FontWeight.Bold),
    titleLarge = arabicStyle(22, 32, FontWeight.Bold),
    titleMedium = arabicStyle(17, 26, FontWeight.SemiBold),
    titleSmall = arabicStyle(15, 24, FontWeight.SemiBold),
    bodyLarge = arabicStyle(16, 26),
    bodyMedium = arabicStyle(14, 23),
    bodySmall = arabicStyle(12, 20),
    labelLarge = arabicStyle(14, 22, FontWeight.SemiBold),
    labelMedium = arabicStyle(12, 18, FontWeight.Medium),
    labelSmall = arabicStyle(11, 16, FontWeight.Medium),
)
