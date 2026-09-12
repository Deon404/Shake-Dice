package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.dice.DieTheme
import com.example.dice.PipPosition

/**
 * Premium physical die component with mathematical pip positioning.
 * Features customizable physical themes (Red, White, Black), 3D specular depth,
 * distinct borders, realistic cast shadows, and spring settle animations.
 */
@Composable
fun DiceView(
  value: Int,
  modifier: Modifier = Modifier,
  theme: DieTheme = DieTheme.CRIMSON_RED,
  sizeDp: Dp = 90.dp,
  rotationAngle: Float = 0f,
  scaleFactor: Float = 1f,
  offsetY: Dp = 0.dp,
  isRolling: Boolean = false
) {
  val pipDescription = stringResource(R.string.dice_pip_content_description, value)
  val cornerRadiusDp = sizeDp * 0.22f
  val shape = RoundedCornerShape(cornerRadiusDp)

  // Animated settle spring
  val animatedRotation by animateFloatAsState(
    targetValue = rotationAngle,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "die_rotation"
  )

  val animatedScale by animateFloatAsState(
    targetValue = scaleFactor,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "die_scale"
  )

  val surfaceBrush = Brush.linearGradient(
    colors = listOf(theme.surfaceTop, theme.surfaceBottom),
    start = Offset.Zero,
    end = Offset.Infinite
  )

  Box(
    modifier = modifier
      .offset(y = offsetY)
      .rotate(animatedRotation)
      .scale(animatedScale)
      .size(sizeDp)
      .shadow(
        elevation = if (isRolling) 16.dp else 9.dp,
        shape = shape,
        ambientColor = Color(0x38000000),
        spotColor = theme.shadowColor
      )
      .clip(shape)
      .background(surfaceBrush)
      .border(theme.borderWidthDp.dp, theme.borderColor, shape)
      .semantics { contentDescription = pipDescription }
      .testTag("dice_view_$value")
  ) {
    Canvas(modifier = Modifier.size(sizeDp)) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val pipRadius = canvasWidth * 0.092f

      // 3D Top-edge specular highlight (curved acrylic sheen)
      drawRoundRect(
        color = theme.specularHighlight,
        topLeft = Offset(canvasWidth * 0.08f, canvasHeight * 0.05f),
        size = Size(canvasWidth * 0.84f, canvasHeight * 0.16f),
        cornerRadius = CornerRadius(canvasWidth * 0.14f, canvasWidth * 0.14f)
      )

      // Active pips based on the mathematical 3x3 layout
      val activePositions = PipPosition.forValue(value)
      val margin = canvasWidth * 0.23f
      val span = canvasWidth - (margin * 2f)

      for (pos in activePositions) {
        val cx = margin + (pos.col * (span / 2f))
        val cy = margin + (pos.row * (span / 2f))

        // Pip inset depth shadow (drilled hole effect)
        drawCircle(
          color = theme.pipInsetShadow,
          radius = pipRadius + 0.8f,
          center = Offset(cx, cy + 1.2f)
        )

        // Pip main fill
        drawCircle(
          color = theme.pipColor,
          radius = pipRadius,
          center = Offset(cx, cy)
        )

        // Tiny specular dot on dark pips for lacquer shine
        if (theme == DieTheme.PORCELAIN_WHITE) {
          drawCircle(
            color = Color(0x55FFFFFF),
            radius = pipRadius * 0.26f,
            center = Offset(cx - pipRadius * 0.28f, cy - pipRadius * 0.28f)
          )
        }
      }
    }
  }
}
