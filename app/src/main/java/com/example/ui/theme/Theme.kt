package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StonicDarkColorScheme = darkColorScheme(
    primary = StonicCyan,
    onPrimary = StonicDarkBg,
    primaryContainer = StonicDarkSurfaceVariant,
    onPrimaryContainer = StonicCyanBright,
    secondary = StonicNeonPurple,
    onSecondary = StonicDarkBg,
    secondaryContainer = StonicDarkSurfaceVariant,
    onSecondaryContainer = StonicCyanBright,
    tertiary = StonicBlueGlow,
    onTertiary = StonicDarkBg,
    background = StonicDarkBg,
    onBackground = StonicTextPrimary,
    surface = StonicDarkSurface,
    onSurface = StonicTextPrimary,
    surfaceVariant = StonicDarkSurfaceVariant,
    onSurfaceVariant = StonicTextSecondary,
    outline = StonicCardBorder,
    outlineVariant = StonicCardBorderGlow
)

private val StonicLightColorScheme = lightColorScheme(
    primary = StonicBlueGlow,
    onPrimary = StonicDarkBg,
    primaryContainer = StonicCyanBright,
    onPrimaryContainer = StonicDarkBg,
    secondary = StonicNeonPurple,
    onSecondary = StonicTextPrimary,
    background = StonicDarkBg, // Keep dark futuristic look even in light mode for JARVIS aesthetic
    onBackground = StonicTextPrimary,
    surface = StonicDarkSurface,
    onSurface = StonicTextPrimary
)

@Composable
fun StonicTheme(
    darkTheme: Boolean = true, // Default to dark futuristic JARVIS theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StonicDarkColorScheme else StonicLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = StonicDarkBg.toArgb()
                window.navigationBarColor = StonicDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
