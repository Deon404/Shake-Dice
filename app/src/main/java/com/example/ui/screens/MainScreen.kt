package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.dice.DieTheme
import com.example.ui.components.DiceCountSelector
import com.example.ui.components.DiceRow
import com.example.ui.components.RollButton
import com.example.ui.components.RollHistorySheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.ShakeHint
import com.example.ui.theme.ArenaBackgroundLight
import com.example.ui.theme.ArenaBorderLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.FluidDarkGrey
import com.example.ui.theme.PrimaryTextLight
import com.example.ui.theme.SecondaryTextLight
import com.example.ui.theme.SystemBackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  viewModel: DiceViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  var isSettingsOpen by remember { mutableStateOf(false) }
  var isHistoryOpen by remember { mutableStateOf(false) }

  val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(SystemBackgroundLight)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("main_screen")
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isCompactHeight = maxHeight < 680.dp

      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // TOP APP HEADER (iOS frosted styling)
        Row(
          modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = if (isCompactHeight) 8.dp else 16.dp)
            .testTag("app_header"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x30000000))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Text(
              text = stringResource(R.string.app_name),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp,
              color = PrimaryTextLight
            )
          }

          // Header action buttons (History & Settings)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { isHistoryOpen = true },
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xE6FFFFFF))
                .border(1.dp, BorderLight, CircleShape)
                .testTag("history_icon_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = stringResource(R.string.history),
                tint = PrimaryTextLight
              )
            }

            IconButton(
              onClick = { isSettingsOpen = true },
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xE6FFFFFF))
                .border(1.dp, BorderLight, CircleShape)
                .testTag("settings_icon_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = PrimaryTextLight
              )
            }
          }
        }

        // MAIN ARENA
        Column(
          modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .padding(vertical = if (isCompactHeight) 8.dp else 16.dp)
            .testTag("arena_section"),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Unified iOS Liquid Arena Screen / Tray (Light tint of A0C4FF so dice pop and focus stays in arena)
          val arenaShape = RoundedCornerShape(26.dp)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .shadow(
                elevation = 6.dp,
                shape = arenaShape,
                ambientColor = Color(0x1A1E3A8A),
                spotColor = Color(0x281E3A8A)
              )
              .clip(arenaShape)
              .background(ArenaBackgroundLight) // Soft light tint derived from A0C4FF
              .border(
                width = 2.dp,
                color = ArenaBorderLight, // Elegant matching light blue border line
                shape = arenaShape
              )
              .padding(
                vertical = if (isCompactHeight) 18.dp else 24.dp,
                horizontal = 14.dp
              )
              .testTag("arena_tray"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              // Dice Row inside Arena
              DiceRow(
                diceValues = uiState.diceValues,
                isRolling = uiState.isRolling,
                rotationAngles = uiState.rotationAngles,
                scaleFactors = uiState.scaleFactors,
                verticalOffsets = uiState.verticalOffsets,
                modifier = Modifier.heightIn(min = if (isCompactHeight) 104.dp else 122.dp)
              )

              Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 18.dp))

              // Clean dividing border line between dice and sum
              Box(
                modifier = Modifier
                  .fillMaxWidth(0.75f)
                  .height(1.5.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE2EAF6))
              )

              Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 16.dp))

              // Prominent Total Sum inside the Arena rounded box
              AnimatedContent(
                targetState = uiState.totalSum,
                transitionSpec = {
                  fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "total_sum_animation"
              ) { sum ->
                Text(
                  text = "$sum",
                  fontSize = if (uiState.diceCount > 1) 54.sp else 46.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = (-1.5).sp,
                  color = PrimaryTextLight,
                  modifier = Modifier.testTag("total_sum_display")
                )
              }

              Text(
                text = if (uiState.diceCount > 1) stringResource(R.string.total_sum) else stringResource(R.string.single_die_result),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = SecondaryTextLight,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }
        }

        // DICE COUNT SELECTOR SECTION
        Column(
          modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .padding(vertical = if (isCompactHeight) 10.dp else 18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          DiceCountSelector(
            selectedCount = uiState.diceCount,
            onCountSelected = { viewModel.setDiceCount(it) },
            enabled = !uiState.isRolling
          )
          Text(
            text = stringResource(R.string.dice_count),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 10.dp)
          )
        }

        // BOTTOM CONTROLS (Roll Button & Haptic Hint)
        Column(
          modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .padding(top = if (isCompactHeight) 10.dp else 16.dp, bottom = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          RollButton(
            onClick = { viewModel.rollDice(isFromShake = false) },
            isRolling = uiState.isRolling
          )

          ShakeHint(
            isSensorSupported = uiState.isSensorSupported,
            isShakeEnabled = uiState.isShakeToRollEnabled
          )
        }
      }
    }

    // Settings Modal Sheet
    if (isSettingsOpen) {
      SettingsSheet(
        sheetState = settingsSheetState,
        onDismiss = { isSettingsOpen = false },
        isShakeEnabled = uiState.isShakeToRollEnabled,
        onToggleShake = { viewModel.toggleShake(it) },
        isHapticsEnabled = uiState.isHapticsEnabled,
        onToggleHaptics = { viewModel.toggleHaptics(it) },
        sensitivity = uiState.shakeSensitivity,
        onSelectSensitivity = { viewModel.setShakeSensitivity(it) },
        diceCount = uiState.diceCount,
        onSelectDiceCount = { viewModel.setDiceCount(it) },
        isSensorSupported = uiState.isSensorSupported,
        isHapticsSupported = uiState.isHapticsSupported
      )
    }

    // History Modal Sheet
    if (isHistoryOpen) {
      RollHistorySheet(
        sheetState = historySheetState,
        history = uiState.history,
        onDismiss = { isHistoryOpen = false },
        onClearHistory = { viewModel.clearHistory() }
      )
    }
  }
}
