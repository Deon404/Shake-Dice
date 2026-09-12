package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.FluidDarkGrey
import com.example.ui.theme.SecondaryTextLight

/**
 * iOS-style segmented controller for selecting dice count (1, 2, or 3).
 * Features an elevated white pill indicator with clean grey neutral tones.
 */
@Composable
fun DiceCountSelector(
  selectedCount: Int,
  onCountSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  val options = listOf(1, 2, 3)
  val containerShape = RoundedCornerShape(16.dp)
  val itemShape = RoundedCornerShape(12.dp)
  val haptic = LocalHapticFeedback.current

  Box(
    modifier = modifier
      .width(236.dp)
      .height(50.dp)
      .shadow(2.dp, containerShape, ambientColor = Color(0x0C1E3A8A), spotColor = Color(0x141E3A8A))
      .clip(containerShape)
      .background(Color(0xFFD3E2F4))
      .border(1.dp, Color(0x66FFFFFF), containerShape)
      .padding(4.dp)
      .testTag("dice_count_selector"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      options.forEach { count ->
        val isSelected = count == selectedCount
        val interactionSource = remember { MutableInteractionSource() }

        val textColor by animateColorAsState(
          targetValue = if (isSelected) FluidDarkGrey else SecondaryTextLight,
          animationSpec = spring(stiffness = Spring.StiffnessMedium),
          label = "selector_text_color"
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(itemShape)
            .then(
              if (isSelected) {
                Modifier
                  .shadow(
                    elevation = 3.dp,
                    shape = itemShape,
                    ambientColor = Color(0x1F000000),
                    spotColor = Color(0x28000000)
                  )
                  .background(Color.White)
              } else {
                Modifier
              }
            )
            .clickable(
              interactionSource = interactionSource,
              indication = ripple(bounded = true),
              enabled = enabled,
              onClick = {
                if (count != selectedCount) {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onCountSelected(count)
              }
            )
            .semantics {
              role = Role.RadioButton
              selected = isSelected
              contentDescription = "$count ${if (count == 1) "die" else "dice"}"
            }
            .testTag("dice_count_button_$count"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$count",
            fontSize = 17.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
          )
        }
      }
    }
  }
}
