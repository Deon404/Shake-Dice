package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderLight
import com.example.ui.theme.FluidDarkGrey
import com.example.ui.theme.SecondaryTextLight

/**
 * Subtle secondary information card displayed at the bottom of the screen.
 * Styled with iOS fluid grey card and clean border.
 */
@Composable
fun ShakeHint(
  isSensorSupported: Boolean,
  isShakeEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  val cardShape = RoundedCornerShape(16.dp)
  val message = when {
    !isSensorSupported -> stringResource(R.string.haptic_hint_unavailable)
    !isShakeEnabled -> stringResource(R.string.haptic_hint_disabled)
    else -> stringResource(R.string.haptic_hint_active)
  }

  Box(
    modifier = modifier
      .widthIn(max = 380.dp)
      .fillMaxWidth()
      .clip(cardShape)
      .background(Color.White)
      .border(1.2.dp, BorderLight, cardShape)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("shake_hint_card")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isSensorSupported && isShakeEnabled) Icons.Outlined.Vibration else Icons.Outlined.ScreenRotation,
          contentDescription = null,
          tint = if (isSensorSupported && isShakeEnabled) FluidDarkGrey else SecondaryTextLight,
          modifier = Modifier.size(18.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.haptic_hint_title),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = Color(0xFF94A3B8)
        )
        Text(
          text = message,
          fontSize = 13.sp,
          color = Color(0xFF1E293B),
          lineHeight = 17.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }
  }
}
