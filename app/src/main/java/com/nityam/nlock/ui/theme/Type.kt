package com.nityam.nlock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Note: Using default SansSerif because ui-text-google-fonts dependency is not allowed by plan rules.
private val InterFontFamily = FontFamily.SansSerif

/**
 * NLock typography using Inter.
 * Tabular figures are recommended for PIN digits, but require font support.
 */
internal val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 57.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 32.sp,
        letterSpacing = (-0.02).em,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    )
)