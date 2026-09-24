package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = TurquoiseDark,
  onPrimary = Color.White,
  primaryContainer = TurquoiseLight.copy(alpha = 0.25f),
  onPrimaryContainer = NavyDark,
  secondary = NavyDark,
  onSecondary = Color.White,
  secondaryContainer = NavyLight.copy(alpha = 0.15f),
  onSecondaryContainer = NavyDark,
  tertiary = BrightBlue,
  onTertiary = Color.White,
  background = NeutralBgLight,
  onBackground = TextPrimaryLight,
  surface = NeutralCardLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = TextSecondaryLight,
  outline = NeutralCardBorder,
  error = CriticalCoral,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = TurquoisePrimary,
  onPrimary = NavyDark,
  primaryContainer = NavySurface,
  onPrimaryContainer = TurquoiseLight,
  secondary = BrightBlue,
  onSecondary = NavyDark,
  secondaryContainer = NavyLight,
  onSecondaryContainer = Color.White,
  tertiary = WarningAmber,
  onTertiary = NavyDark,
  background = NeutralBgDark,
  onBackground = TextPrimaryDark,
  surface = NeutralCardDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = NavySurface,
  onSurfaceVariant = TextSecondaryDark,
  outline = NeutralCardBorderDark,
  error = CriticalCoral,
  onError = Color.White
)

@Composable
fun AkyolInventoryTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  AkyolInventoryTheme(darkTheme = darkTheme, content = content)
}
