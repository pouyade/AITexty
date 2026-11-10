package com.dpouya.messages.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, SYSTEM }

val LocalMessagesColors = staticCompositionLocalOf { LightPalette }

object MessagesThemeTokens {
    val colors: MessagesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMessagesColors.current
}

@Composable
fun MessagesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.mainBackground,
            surface = colors.surface,
            onBackground = colors.primaryText,
            onSurface = colors.primaryText,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.mainBackground,
            surface = colors.surface,
            onBackground = colors.primaryText,
            onSurface = colors.primaryText,
            error = colors.error,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalMessagesColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            content = content,
        )
    }
}
