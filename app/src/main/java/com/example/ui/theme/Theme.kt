package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianDarkPrimary,
    onPrimary = ObsidianDarkOnPrimary,
    primaryContainer = ObsidianDarkPrimaryContainer,
    onPrimaryContainer = ObsidianDarkOnPrimaryContainer,
    secondary = ObsidianDarkSecondary,
    onSecondary = ObsidianDarkOnSecondary,
    secondaryContainer = ObsidianDarkSecondaryContainer,
    onSecondaryContainer = ObsidianDarkOnSecondaryContainer,
    tertiary = ObsidianDarkTertiary,
    onTertiary = ObsidianDarkOnTertiary,
    tertiaryContainer = ObsidianDarkTertiaryContainer,
    onTertiaryContainer = ObsidianDarkOnTertiaryContainer,
    background = ObsidianDarkBg,
    onBackground = ObsidianDarkOnSurface,
    surface = ObsidianDarkSurface,
    onSurface = ObsidianDarkOnSurface,
    surfaceVariant = ObsidianDarkSurfaceVariant,
    onSurfaceVariant = ObsidianDarkOnSurfaceVariant,
    outline = ObsidianDarkOutline,
    outlineVariant = ObsidianDarkOutlineVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val ObsidianLightColorScheme = lightColorScheme(
    primary = ObsidianLightPrimary,
    onPrimary = ObsidianLightOnPrimary,
    primaryContainer = ObsidianLightPrimaryContainer,
    onPrimaryContainer = ObsidianLightOnPrimaryContainer,
    secondary = ObsidianLightSecondary,
    onSecondary = ObsidianLightOnSecondary,
    secondaryContainer = ObsidianLightSecondaryContainer,
    onSecondaryContainer = ObsidianLightOnSecondaryContainer,
    tertiary = ObsidianLightTertiary,
    onTertiary = ObsidianLightOnTertiary,
    tertiaryContainer = ObsidianLightTertiaryContainer,
    onTertiaryContainer = ObsidianLightOnTertiaryContainer,
    background = ObsidianLightBg,
    onBackground = ObsidianLightOnSurface,
    surface = ObsidianLightSurface,
    onSurface = ObsidianLightOnSurface,
    surfaceVariant = ObsidianLightSurfaceVariant,
    onSurfaceVariant = ObsidianLightOnSurfaceVariant,
    outline = ObsidianLightOutline,
    outlineVariant = ObsidianLightOutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun PlayZoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else ObsidianLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

