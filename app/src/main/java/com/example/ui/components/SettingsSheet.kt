package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.sensors.ShakeSensitivity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ArenaBackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackgroundLight
import com.example.ui.theme.FluidDarkGrey
import com.example.ui.theme.PrimaryTextLight
import com.example.ui.theme.SecondaryTextLight
import com.example.ui.theme.SystemBackgroundLight
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
  sheetState: SheetState,
  onDismiss: () -> Unit,
  isShakeEnabled: Boolean,
  onToggleShake: (Boolean) -> Unit,
  isHapticsEnabled: Boolean,
  onToggleHaptics: (Boolean) -> Unit,
  sensitivity: ShakeSensitivity,
  onSelectSensitivity: (ShakeSensitivity) -> Unit,
  diceCount: Int,
  onSelectDiceCount: (Int) -> Unit,
  isSensorSupported: Boolean,
  isHapticsSupported: Boolean,
  liveMotionIntensity: Float = 0f
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SystemBackgroundLight,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 8.dp)
          .size(width = 40.dp, height = 4.5.dp)
          .clip(CircleShape)
          .background(Color(0xFFBACAE0))
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 40.dp)
        .testTag("settings_sheet")
    ) {
      // Header with Cupertino typography and dismiss button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = stringResource(R.string.settings),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = PrimaryTextLight
          )
          Text(
            text = "Preferences & Controls",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = SecondaryTextLight
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFD8E5F7))
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("settings_done_button")
          ) {
            Text(
              text = "Done",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = FluidDarkGrey
            )
          }
        }
      }

      // Group 1: Gestures & Feedback Card
      SectionHeader(title = "INTERACTION & FEEDBACK")
      SettingsCard {
        // Shake-to-roll toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          LeadingIconBox(
            icon = Icons.Outlined.Sensors,
            tint = if (isSensorSupported && isShakeEnabled) FluidDarkGrey else SecondaryTextLight,
            bgColor = if (isSensorSupported && isShakeEnabled) Color(0xFFDCE8FA) else Color(0xFFEFF3F8)
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.shake_to_roll),
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isSensorSupported) PrimaryTextLight else SecondaryTextLight
            )
            Text(
              text = if (isSensorSupported) stringResource(R.string.shake_to_roll_desc) else stringResource(R.string.sensor_unavailable),
              fontSize = 12.sp,
              lineHeight = 16.sp,
              color = SecondaryTextLight,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Switch(
            checked = isShakeEnabled && isSensorSupported,
            onCheckedChange = { if (isSensorSupported) onToggleShake(it) },
            enabled = isSensorSupported,
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = FluidDarkGrey,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color(0xFFD6DFEB)
            ),
            modifier = Modifier.testTag("toggle_shake_switch")
          )
        }

        HorizontalDivider(
          color = Color(0xFFE8EFF8),
          thickness = 1.dp,
          modifier = Modifier.padding(start = 58.dp, end = 16.dp)
        )

        // Haptic feedback toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          LeadingIconBox(
            icon = Icons.Outlined.Vibration,
            tint = if (isHapticsEnabled) FluidDarkGrey else SecondaryTextLight,
            bgColor = if (isHapticsEnabled) Color(0xFFDCE8FA) else Color(0xFFEFF3F8)
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.haptic_feedback),
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = PrimaryTextLight
            )
            Text(
              text = stringResource(R.string.haptic_feedback_desc),
              fontSize = 12.sp,
              lineHeight = 16.sp,
              color = SecondaryTextLight,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Switch(
            checked = isHapticsEnabled,
            onCheckedChange = onToggleHaptics,
            enabled = isHapticsSupported,
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = FluidDarkGrey,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color(0xFFD6DFEB)
            ),
            modifier = Modifier.testTag("toggle_haptics_switch")
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Group 2: Shake Sensitivity
      SectionHeader(title = stringResource(R.string.shake_sensitivity).uppercase())
      SettingsCard {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Card Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
          ) {
            LeadingIconBox(
              icon = Icons.Outlined.Tune,
              tint = FluidDarkGrey,
              bgColor = Color(0xFFDCE8FA)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Threshold Tuning",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryTextLight
              )
              Text(
                text = "Adjust motion intensity to trigger rolls",
                fontSize = 12.sp,
                color = SecondaryTextLight
              )
            }
          }

          // Elevated Pill Segmented Selector matching DiceCountSelector design
          val containerShape = RoundedCornerShape(16.dp)
          val itemShape = RoundedCornerShape(12.dp)
          val haptic = LocalHapticFeedback.current

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .shadow(2.dp, containerShape, ambientColor = Color(0x0C1E3A8A), spotColor = Color(0x141E3A8A))
              .clip(containerShape)
              .background(Color(0xFFD3E2F4))
              .border(1.dp, Color(0x66FFFFFF), containerShape)
              .padding(4.dp)
              .testTag("shake_sensitivity_selector"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              ShakeSensitivity.values().forEach { item ->
                val isSelected = item == sensitivity
                val interactionSource = remember { MutableInteractionSource() }

                val textColor by animateColorAsState(
                  targetValue = if (isSelected) FluidDarkGrey else SecondaryTextLight,
                  animationSpec = spring(stiffness = Spring.StiffnessMedium),
                  label = "sensitivity_text_color"
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
                      onClick = {
                        if (item != sensitivity) {
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          onSelectSensitivity(item)
                        }
                      }
                    )
                    .semantics {
                      role = Role.RadioButton
                      selected = isSelected
                      contentDescription = "${item.displayName} sensitivity"
                    }
                    .testTag("sensitivity_${item.name.lowercase()}"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = item.displayName,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Clean descriptive text
          Text(
            text = sensitivity.description,
            fontSize = 13.sp,
            color = SecondaryTextLight,
            modifier = Modifier.padding(start = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Group 3: Hardware Sensor Diagnostic
      SectionHeader(title = "SYSTEM & SENSOR STATUS")
      SettingsCard {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          LeadingIconBox(
            icon = if (isSensorSupported) Icons.Outlined.Devices else Icons.Outlined.Warning,
            tint = if (isSensorSupported) Color(0xFF10B981) else Color(0xFFF59E0B),
            bgColor = if (isSensorSupported) Color(0xFFE6F9F0) else Color(0xFFFEF3C7)
          )
          Column {
            Text(
              text = stringResource(R.string.motion_sensor_status),
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = PrimaryTextLight
            )
            Text(
              text = if (isSensorSupported) stringResource(R.string.sensor_active) else stringResource(R.string.sensor_unavailable),
              fontSize = 12.sp,
              color = SecondaryTextLight,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Group 4: About & Developer Credit (Sidon Studio)
      SectionHeader(title = "ABOUT & CREDITS")
      SettingsCard {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            LeadingIconBox(
              icon = Icons.Outlined.Info,
              tint = FluidDarkGrey,
              bgColor = Color(0xFFDCE8FA)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = stringResource(R.string.app_name),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTextLight
              )
              Text(
                text = stringResource(R.string.tagline),
                fontSize = 12.sp,
                color = SecondaryTextLight
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          HorizontalDivider(
            color = Color(0xFFE8EFF8),
            thickness = 1.dp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Developer Credit Card Badge for Sidon Studio
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(Color(0xFFEBF3FC), Color(0xFFF3F8FE))
                )
              )
              .border(1.dp, Color(0xFFD0E0F5), RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(FluidDarkGrey),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.WorkspacePremium,
                  contentDescription = "Developer Icon",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = stringResource(R.string.developer_credit_label).uppercase(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  letterSpacing = 1.sp,
                  color = SecondaryTextLight
                )
                Text(
                  text = stringResource(R.string.developer_credit_name),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.2).sp,
                  color = PrimaryTextLight
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Version metadata
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Shake Dice Engine",
              fontSize = 11.sp,
              color = SecondaryTextLight
            )
            Text(
              text = stringResource(R.string.app_version),
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryTextLight
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LeadingIconBox(
  icon: ImageVector,
  tint: Color,
  bgColor: Color
) {
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.sp,
    color = SecondaryTextLight,
    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
  )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
  val shape = RoundedCornerShape(18.dp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(
        elevation = 2.dp,
        shape = shape,
        ambientColor = Color(0x0C1E3A8A),
        spotColor = Color(0x141E3A8A)
      )
      .clip(shape)
      .background(CardBackgroundLight)
      .border(1.dp, BorderLight, shape)
  ) {
    Column { content() }
  }
}

