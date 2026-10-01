package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepObsidian,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricSky,
    onSecondary = DeepObsidian,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = ElectricSky,
    tertiary = SunsetAmber,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorder,
    error = NeonCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Cinema OLED theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
