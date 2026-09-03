package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val DarkRetroColorScheme = darkColorScheme(
    primary = RetroTerracottaDark,
    onPrimary = RetroTerracottaOnDark,
    primaryContainer = RetroTerracottaContainerDark,
    onPrimaryContainer = RetroTerracottaOnContainerDark,
    secondary = RetroCognacDark,
    onSecondary = RetroCognacOnDark,
    secondaryContainer = RetroCognacContainerDark,
    onSecondaryContainer = RetroCognacOnContainerDark,
    tertiary = RetroForestDark,
    onTertiary = RetroForestOnDark,
    tertiaryContainer = RetroForestContainerDark,
    onTertiaryContainer = RetroForestOnContainerDark,
    background = RetroBackgroundDark,
    surface = RetroSurfaceDark,
    surfaceVariant = RetroSurfaceVariantDark,
    onBackground = RetroOnSurfaceDark,
    onSurface = RetroOnSurfaceDark,
    onSurfaceVariant = RetroOnSurfaceVariantDark,
    outline = RetroOutlineDark
)

private val LightRetroColorScheme = lightColorScheme(
    primary = RetroTerracottaPrimary,
    onPrimary = RetroTerracottaOnPrimary,
    primaryContainer = RetroTerracottaContainer,
    onPrimaryContainer = RetroTerracottaOnContainer,
    secondary = RetroCognacSecondary,
    onSecondary = RetroCognacOnSecondary,
    secondaryContainer = RetroCognacContainer,
    onSecondaryContainer = RetroCognacOnContainer,
    tertiary = RetroForestTertiary,
    onTertiary = RetroForestOnTertiary,
    tertiaryContainer = RetroForestContainer,
    onTertiaryContainer = RetroForestOnContainer,
    background = RetroBackgroundLight,
    surface = RetroSurfaceLight,
    surfaceVariant = RetroSurfaceVariantLight,
    onBackground = RetroOnSurfaceLight,
    onSurface = RetroOnSurfaceLight,
    onSurfaceVariant = RetroOnSurfaceVariantLight,
    outline = RetroOutlineLight
)

@Composable
fun WordSmartTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkRetroColorScheme else LightRetroColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

