package com.example.dice

import androidx.compose.ui.graphics.Color

/**
 * Visual styling theme for authentic physical dice.
 * Matches the Red, White, and Black physical dice styling.
 */
enum class DieTheme(
  val displayName: String,
  val surfaceTop: Color,
  val surfaceBottom: Color,
  val borderColor: Color,
  val borderWidthDp: Float,
  val pipColor: Color,
  val pipInsetShadow: Color,
  val specularHighlight: Color,
  val shadowColor: Color,
  val badgeBackground: Color,
  val badgeTextColor: Color
) {
  CRIMSON_RED(
    displayName = "Crimson Red",
    surfaceTop = Color(0xFFEF4444),
    surfaceBottom = Color(0xFFB91C1C),
    borderColor = Color(0xFF7F1D1D),
    borderWidthDp = 1.8f,
    pipColor = Color(0xFFFFFFFF),
    pipInsetShadow = Color(0x60000000),
    specularHighlight = Color(0x4DFFFFFF),
    shadowColor = Color(0x40DC2626),
    badgeBackground = Color(0xFFFEE2E2),
    badgeTextColor = Color(0xFF991B1B)
  ),
  PORCELAIN_WHITE(
    displayName = "Porcelain White",
    surfaceTop = Color(0xFFFFFFFF),
    surfaceBottom = Color(0xFFE2E8F0),
    borderColor = Color(0xFF94A3B8),
    borderWidthDp = 2.0f,
    pipColor = Color(0xFF1E293B),
    pipInsetShadow = Color(0x30000000),
    specularHighlight = Color(0x99FFFFFF),
    shadowColor = Color(0x35000000),
    badgeBackground = Color(0xFFF1F5F9),
    badgeTextColor = Color(0xFF334155)
  ),
  OBSIDIAN_BLACK(
    displayName = "Obsidian Black",
    surfaceTop = Color(0xFF334155),
    surfaceBottom = Color(0xFF0F172A),
    borderColor = Color(0xFF475569),
    borderWidthDp = 1.8f,
    pipColor = Color(0xFFFFFFFF),
    pipInsetShadow = Color(0x70000000),
    specularHighlight = Color(0x30FFFFFF),
    shadowColor = Color(0x60000000),
    badgeBackground = Color(0xFFE2E8F0),
    badgeTextColor = Color(0xFF0F172A)
  );

  companion object {
    /**
     * Determines the die theme based on its position index.
     * Index 0: Crimson Red
     * Index 1: Porcelain White
     * Index 2: Obsidian Black
     */
    fun forIndex(index: Int): DieTheme = when (index % 3) {
      0 -> CRIMSON_RED
      1 -> PORCELAIN_WHITE
      2 -> OBSIDIAN_BLACK
      else -> CRIMSON_RED
    }
  }
}
