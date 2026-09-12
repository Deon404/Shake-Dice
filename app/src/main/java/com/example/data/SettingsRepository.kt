package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sensors.ShakeSensitivity

class SettingsRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("shake_dice_settings", Context.MODE_PRIVATE)

  var isShakeToRollEnabled: Boolean
    get() = prefs.getBoolean(KEY_SHAKE_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_SHAKE_ENABLED, value).apply()

  var isHapticsEnabled: Boolean
    get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

  var shakeSensitivity: ShakeSensitivity
    get() {
      val name = prefs.getString(KEY_SENSITIVITY, ShakeSensitivity.MEDIUM.name)
      return try {
        ShakeSensitivity.valueOf(name ?: ShakeSensitivity.MEDIUM.name)
      } catch (_: Exception) {
        ShakeSensitivity.MEDIUM
      }
    }
    set(value) = prefs.edit().putString(KEY_SENSITIVITY, value.name).apply()

  var diceCount: Int
    get() = prefs.getInt(KEY_DICE_COUNT, 2).coerceIn(1, 3)
    set(value) = prefs.edit().putInt(KEY_DICE_COUNT, value.coerceIn(1, 3)).apply()

  companion object {
    private const val KEY_SHAKE_ENABLED = "shake_enabled"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private const val KEY_SENSITIVITY = "shake_sensitivity"
    private const val KEY_DICE_COUNT = "dice_count"
  }
}
