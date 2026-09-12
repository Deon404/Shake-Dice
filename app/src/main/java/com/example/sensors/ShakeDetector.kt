package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Robust, battery-friendly Shake Detector using Android's native SensorManager.
 * Detects deliberate wrist/arm shaking while filtering out walking, phone placement, and scrolling.
 */
class ShakeDetector(
  context: Context,
  private val onShake: () -> Unit
) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

  val isSupported: Boolean
    get() = accelerometer != null

  var isEnabled: Boolean = true
  var sensitivity: ShakeSensitivity = ShakeSensitivity.DEFAULT

  // Debounce & cooldown control
  private var windowStartTime: Long = 0L
  private var shakeCount: Int = 0
  private var lastTriggerTime: Long = 0L

  // Low-pass filtered baseline
  private var prevX = 0f
  private var prevY = 0f
  private var prevZ = 0f
  private var isInitialized = false

  companion object {
    private const val WINDOW_MS = 650L
    private const val COOLDOWN_MS = 750L
    private const val MIN_REVERSALS = 2
  }

  fun start() {
    if (!isSupported || !isEnabled) return
    isInitialized = false
    shakeCount = 0
    windowStartTime = 0L
    accelerometer?.let {
      sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
  }

  fun stop() {
    if (!isSupported) return
    try {
      sensorManager?.unregisterListener(this)
    } catch (_: Exception) {
      // Graceful unregister
    }
    shakeCount = 0
    windowStartTime = 0L
    isInitialized = false
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || !isEnabled) return

    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]

    if (!isInitialized) {
      prevX = x
      prevY = y
      prevZ = z
      isInitialized = true
      return
    }

    // High-pass filter acceleration delta (independent of static gravity orientation)
    val deltaX = x - prevX
    val deltaY = y - prevY
    val deltaZ = z - prevZ

    prevX = x
    prevY = y
    prevZ = z

    // Calculate acceleration delta in G units (1G ≈ 9.80665 m/s²)
    val deltaMagnitude = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
    val deltaG = deltaMagnitude / SensorManager.GRAVITY_EARTH

    val now = System.currentTimeMillis()

    // Enforce cooldown so consecutive shakes don't spam while previous roll is active
    if (now - lastTriggerTime < COOLDOWN_MS) {
      shakeCount = 0
      windowStartTime = 0L
      return
    }

    // Check if acceleration delta exceeds sensitivity threshold
    if (deltaG >= sensitivity.thresholdG) {
      if (windowStartTime == 0L || (now - windowStartTime > WINDOW_MS)) {
        // Start a new shaking evaluation window
        windowStartTime = now
        shakeCount = 1
      } else {
        shakeCount++
        // Trigger on reaching minimum directional shakes within window
        if (shakeCount >= MIN_REVERSALS) {
          lastTriggerTime = now
          shakeCount = 0
          windowStartTime = 0L
          onShake()
        }
      }
    } else if (windowStartTime != 0L && (now - windowStartTime > WINDOW_MS)) {
      // Window expired without enough reversals
      shakeCount = 0
      windowStartTime = 0L
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op for shake detection
  }
}
