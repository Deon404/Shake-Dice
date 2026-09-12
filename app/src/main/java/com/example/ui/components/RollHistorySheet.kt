package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.RollRecord
import com.example.dice.DieTheme
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackgroundLight
import com.example.ui.theme.PrimaryTextLight
import com.example.ui.theme.SecondaryTextLight
import com.example.ui.theme.SystemBackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollHistorySheet(
  sheetState: SheetState,
  history: List<RollRecord>,
  onDismiss: () -> Unit,
  onClearHistory: () -> Unit
) {
  var showConfirmDialog by remember { mutableStateOf(false) }

  if (showConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog = false },
      title = {
        Text(
          text = stringResource(R.string.clear_history),
          fontWeight = FontWeight.Bold,
          color = PrimaryTextLight
        )
      },
      text = {
        Text(
          text = stringResource(R.string.clear_history_confirm),
          color = SecondaryTextLight
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showConfirmDialog = false
            onClearHistory()
          },
          modifier = Modifier.testTag("confirm_clear_history_button")
        ) {
          Text(text = stringResource(R.string.clear), color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDialog = false }) {
          Text(text = stringResource(R.string.cancel), color = SecondaryTextLight)
        }
      },
      containerColor = CardBackgroundLight,
      shape = RoundedCornerShape(18.dp)
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SystemBackgroundLight,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 6.dp)
          .size(width = 36.dp, height = 4.dp)
          .clip(CircleShape)
          .background(Color(0xFFD1D1D6))
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .testTag("history_sheet")
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.history),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryTextLight
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (history.isNotEmpty()) {
            TextButton(
              onClick = { showConfirmDialog = true },
              modifier = Modifier.testTag("clear_history_button")
            ) {
              Text(
                text = "Clear",
                fontSize = 15.sp,
                color = Color(0xFFFF3B30),
                fontWeight = FontWeight.Medium
              )
            }
          }
          TextButton(onClick = onDismiss) {
            Text(
              text = "Done",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = AccentBlue
            )
          }
        }
      }

      if (history.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E5EA)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Casino,
                contentDescription = null,
                tint = SecondaryTextLight,
                modifier = Modifier.size(28.dp)
              )
            }
            Text(
              text = stringResource(R.string.empty_history),
              fontSize = 14.sp,
              color = SecondaryTextLight
            )
          }
        }
      } else {
        val cardShape = RoundedCornerShape(16.dp)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackgroundLight)
            .border(1.dp, BorderLight, cardShape)
        ) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height((minOf(history.size, 6) * 64).dp)
          ) {
            items(history, key = { it.id }) { record ->
              RollHistoryRow(record = record)
              if (record != history.last()) {
                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RollHistoryRow(record: RollRecord) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (record.isShakeTriggered) Icons.Outlined.Vibration else Icons.Outlined.Casino,
          contentDescription = null,
          tint = if (record.isShakeTriggered) AccentBlue else SecondaryTextLight,
          modifier = Modifier.size(18.dp)
        )
      }

      Column {
        Text(
          text = "${record.diceCount} ${if (record.diceCount == 1) "die" else "dice"}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = SecondaryTextLight
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(top = 2.dp)
        ) {
          record.diceValues.forEachIndexed { index, value ->
            val theme = DieTheme.forIndex(index)
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(theme.surfaceBottom)
                .border(0.8.dp, theme.borderColor, RoundedCornerShape(5.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$value",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.pipColor
              )
            }
          }
          Text(
            text = "= ${record.total}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTextLight,
            modifier = Modifier.padding(start = 4.dp)
          )
        }
      }
    }

    Text(
      text = record.formattedTime,
      fontSize = 12.sp,
      color = Color(0xFFAEB0B5)
    )
  }
}
