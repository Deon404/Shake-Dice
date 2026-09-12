package com.example.data

import android.content.Context
import android.content.SharedPreferences

class HistoryRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("shake_dice_history", Context.MODE_PRIVATE)

  fun loadHistory(): List<RollRecord> {
    val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
    return try {
      raw.split(";").filter { it.isNotBlank() }.mapNotNull { entry ->
        val parts = entry.split("|")
        if (parts.size >= 4) {
          val id = parts[0]
          val timestamp = parts[1].toLongOrNull() ?: System.currentTimeMillis()
          val isShake = parts[2].toBooleanStrictOrNull() ?: false
          val values = parts[3].split(",").mapNotNull { it.toIntOrNull() }
          if (values.isNotEmpty()) {
            RollRecord(id = id, timestamp = timestamp, diceValues = values, isShakeTriggered = isShake)
          } else null
        } else null
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveHistory(records: List<RollRecord>) {
    val serialized = records.take(MAX_HISTORY_ENTRIES).joinToString(";") { record ->
      "${record.id}|${record.timestamp}|${record.isShakeTriggered}|${record.diceValues.joinToString(",")}"
    }
    prefs.edit().putString(KEY_HISTORY, serialized).apply()
  }

  fun clearHistory() {
    prefs.edit().remove(KEY_HISTORY).apply()
  }

  companion object {
    private const val KEY_HISTORY = "history_entries"
    const val MAX_HISTORY_ENTRIES = 30
  }
}
