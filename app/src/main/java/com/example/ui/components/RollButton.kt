package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FluidDarkGrey

/**
 * Large premium action button for rolling the dice.
 * Features rounded corners, subtle elevation, a circular white dice badge, and smooth press physics.
 */
@Composable
fun RollButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isRolling: Boolean = false,
  enabled: Boolean = true
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled && !isRolling) 0.965f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "roll_button_scale"
  )

  val buttonShape = RoundedCornerShape(22.dp)
  val buttonBg = if (enabled && !isRolling) FluidDarkGrey else FluidDarkGrey.copy(alpha = 0.65f)
  val buttonText = if (isRolling) stringResource(R.string.rolling) else stringResource(R.string.roll_now)

  Box(
    modifier = modifier
      .widthIn(max = 380.dp)
      .fillMaxWidth()
      .scale(scale)
      .shadow(
        elevation = if (isPressed || isRolling) 3.dp else 8.dp,
        shape = buttonShape,
        ambientColor = Color(0x28000000),
        spotColor = Color(0x38000000)
      )
      .clip(buttonShape)
      .background(buttonBg)
      .border(1.2.dp, Color(0x33FFFFFF), buttonShape)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(bounded = true, color = Color.White),
        enabled = enabled && !isRolling,
        onClick = onClick
      )
      .semantics {
        role = Role.Button
        contentDescription = buttonText
      }
      .testTag("roll_button")
      .padding(horizontal = 22.dp, vertical = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Invisible spacer on the left to balance the icon on the right and keep text perfectly centered
      Box(modifier = Modifier.size(38.dp))

      Text(
        text = buttonText,
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
      )

      // Circular frosted emblem with dice icon on the right
      Box(
        modifier = Modifier
          .size(38.dp)
          .shadow(2.dp, CircleShape, ambientColor = Color(0x20000000))
          .clip(CircleShape)
          .background(Color(0xFF2E3543))
          .border(1.dp, Color(0x30FFFFFF), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Casino,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
