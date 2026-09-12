package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RollRecord(
  val id: String = java.util.UUID.randomUUID().toString(),
  val timestamp: Long = System.currentTimeMillis(),
  val diceValues: List<Int>,
  val isShakeTriggered: Boolean = false
) {
  val total: Int = diceValues.sum()
  val diceCount: Int = diceValues.size

  val formattedFormula: String
    get() = if (diceValues.size == 1) {
      "${diceValues.first()}"
    } else {
      diceValues.joinToString(separator = " + ") + " = $total"
    }

  val formattedTime: String
    get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
