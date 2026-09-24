package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CorporateColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = BackgroundDark,
    primaryContainer = CardSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlue,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentTealLight,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderLight,
    error = StatusFaulty,
    onError = Color.White
)

@Composable
fun AkyolInventoryTheme(
    darkTheme: Boolean = true, // Force modern dark enterprise theme for consistent corporate aesthetics
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CorporateColorScheme,
        typography = Typography,
        content = content
    )
}
