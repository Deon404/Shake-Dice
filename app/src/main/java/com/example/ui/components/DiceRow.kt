package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.dice.DieTheme

/**
 * Centered responsive row of 1, 2, or 3 physical dice with distinct color theming:
 * Die 1: Crimson Red
 * Die 2: Porcelain White
 * Die 3: Obsidian Black
 */
@Composable
fun DiceRow(
  diceValues: List<Int>,
  modifier: Modifier = Modifier,
  isRolling: Boolean = false,
  rotationAngles: List<Float> = emptyList(),
  scaleFactors: List<Float> = emptyList(),
  verticalOffsets: List<Float> = emptyList()
) {
  val count = diceValues.size
  val dieSize = when (count) {
    1 -> 112.dp
    2 -> 94.dp
    else -> 82.dp
  }
  val horizontalSpacing = when (count) {
    1 -> 0.dp
    2 -> 18.dp
    else -> 12.dp
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp)
      .testTag("dice_row"),
    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically
  ) {
    diceValues.forEachIndexed { index, value ->
      val rotation = rotationAngles.getOrElse(index) { 0f }
      val scale = scaleFactors.getOrElse(index) { 1f }
      val offsetY = verticalOffsets.getOrElse(index) { 0f }.dp
      val theme = DieTheme.forIndex(index)

      DiceView(
        value = value,
        theme = theme,
        sizeDp = dieSize,
        rotationAngle = rotation,
        scaleFactor = scale,
        offsetY = offsetY,
        isRolling = isRolling
      )
    }
  }
}
