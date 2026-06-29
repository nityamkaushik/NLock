package com.nityam.nlock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Custom color tokens for NLock's Precision Vault design system.
 */
internal data class NLockColors(
    val base: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val warning: Color,
)

internal val LocalNLockColors = staticCompositionLocalOf {
    NLockColors(
        base = Color.Unspecified,
        surface = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        accent = Color.Unspecified,
        warning = Color.Unspecified,
    )
}

/**
 * Accessor for the current [NLockColors].
 */
internal object NLockTheme {
    internal val colors: NLockColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNLockColors.current
}

/**
 * Main theme wrapper for NLock.
 *
 * Provides custom [NLockColors] via [CompositionLocalProvider] and maps core
 * tokens to [MaterialTheme] so standard components inherit the correct colors.
 */
@Composable
internal fun NLockTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (isDarkTheme) {
        NLockColors(
            base = DarkBase,
            surface = DarkSurface,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            accent = NLockAccent,
            warning = NLockWarningDark,
        )
    } else {
        NLockColors(
            base = LightBase,
            surface = LightSurface,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            accent = NLockAccent,
            warning = NLockWarningLight,
        )
    }

    val materialColorScheme = if (isDarkTheme) {
        darkColorScheme(
            background = colors.base,
            surface = colors.surface,
            primary = colors.accent,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.warning,
        )
    } else {
        lightColorScheme(
            background = colors.base,
            surface = colors.surface,
            primary = colors.accent,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.warning,
        )
    }

    CompositionLocalProvider(LocalNLockColors provides colors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content,
        )
    }
}