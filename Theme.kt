package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentPalette: AppAccentPalette = AppAccentPalette.ORANGE_BLUE,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = accentPalette.primary,
            onPrimary = Color.White,
            secondary = accentPalette.secondary,
            onSecondary = Color.White,
            tertiary = GarageEmeraldGreen,
            background = GarageDarkBg,
            onBackground = Color(0xFFF1F5F9),
            surface = GarageDarkSurface,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = GarageDarkCard,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = GarageDarkCardBorder,
            error = GarageErrorRed
        )
    } else {
        lightColorScheme(
            primary = accentPalette.primary,
            onPrimary = Color.White,
            secondary = accentPalette.secondary,
            onSecondary = Color.White,
            tertiary = Color(0xFF16A34A),
            background = GarageLightBg,
            onBackground = Color(0xFF0F172A),
            surface = GarageLightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = GarageLightCard,
            onSurfaceVariant = Color(0xFF475569),
            outline = GarageLightCardBorder,
            error = GarageErrorRed
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
