package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentBlueNight,
    onPrimary = Color.White,
    primaryContainer = AccentBlueSubtleDark,
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = SecondaryTextDark,
    background = SystemBackgroundDark,
    surface = CardBackgroundDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onBackground = PrimaryTextDark,
    onSurface = PrimaryTextDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF3A3A3C),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlueSubtle,
    onPrimaryContainer = AccentBluePressed,
    secondary = SecondaryTextLight,
    background = SystemBackgroundLight,
    surface = CardBackgroundLight,
    surfaceVariant = Color(0xFFF2F2F7),
    onBackground = PrimaryTextLight,
    onSurface = PrimaryTextLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFE5E5EA),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent Apple-inspired palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

