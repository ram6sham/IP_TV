package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PPColorScheme = darkColorScheme(
  primary = BroadcastCyan,
  onPrimary = Color.Black,
  primaryContainer = SurfaceHighlight,
  onPrimaryContainer = Color.White,
  secondary = ElectricIndigo,
  onSecondary = Color.White,
  tertiary = LiveRed,
  onTertiary = Color.White,
  background = ObsidianBackground,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondary,
  error = LiveRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = PPColorScheme,
    typography = Typography,
    content = content
  )
}
