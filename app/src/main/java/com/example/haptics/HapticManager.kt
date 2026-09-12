package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages tactile haptic feedback for dice interactions.
 * Safely wraps Android Vibrator APIs with fallback for all API levels.
 */
class HapticManager(context: Context) {

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  val isAvailable: Boolean
    get() = vibrator?.hasVibrator() == true

  var isEnabled: Boolean = true

  /**
   * Light/medium haptic for manual button press.
   */
  fun performManualRollHaptic() {
    if (!isEnabled || !isAvailable) return
    vibrate(durationMs = 28, effectId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else null)
  }

  /**
   * Short firm haptic when a physical shake is registered.
   */
  fun performShakeDetectedHaptic() {
    if (!isEnabled || !isAvailable) return
    vibrate(durationMs = 45, effectId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else null)
  }

  /**
   * Subtle landing haptic when the dice settle on the final result.
   */
  fun performDiceLandingHaptic() {
    if (!isEnabled || !isAvailable) return
    vibrate(durationMs = 18, effectId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null)
  }

  private fun vibrate(durationMs: Long, effectId: Int?) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && effectId != null) {
        vibrator?.vibrate(VibrationEffect.createPredefined(effectId))
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) {
      // Graceful fallback for restricted background or device limitations
    }
  }
}
