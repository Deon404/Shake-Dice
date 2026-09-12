package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale
import kotlin.math.max
import kotlin.math.sqrt

interface ShakeMotionListener {
  fun onMotionStarted()
  fun onMotionSettling()
  fun onTelemetryUpdate(debugInfo: MotionDebugInfo)
}

/**
 * Continuous physical motion detector using Android native SensorManager.
 * Emulates the physics of shaking dice in a real cup:
 * - Continuously rolls while the user keeps moving the device
 * - Isolates linear acceleration using high-pass gravity estimation
 * - Rapidly decays upon movement cessation (< 80ms)
 * - Multi-threshold hysteresis: start > continue > stop
 * - Strict 200–350ms stop settle window for immediate responsiveness
 */
class ShakeDetector(
  context: Context,
  private val listener: ShakeMotionListener
) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

  val isSupported: Boolean
    get() = accelerometer != null

  val isGyroSupported: Boolean
    get() = gyroscope != null

  var isEnabled: Boolean = true
  var sensitivity: ShakeSensitivity = ShakeSensitivity.DEFAULT

  // Motion State Machine
  private var currentState: ShakeMotionState = ShakeMotionState.ARMED
  private var isListening: Boolean = false
  @Volatile private var isRollSessionActive: Boolean = false
  @Volatile private var rollSessionId: Long = 0L

  // Dynamic Gravity & Linear Acceleration tracking (m/s²)
  private val gravity = FloatArray(3)
  private var isGravityInitialized = false
  private var prevRawX = 0f
  private var prevRawY = 0f
  private var prevRawZ = 0f
  private var isPrevRawInitialized = false

  // Motion metrics (m/s²)
  private var currentRawAcc = 0f
  private var currentGyroEquivalent = 0f
  private var instantMotion = 0f
  private var filteredMotion = 0f

  // Stop & stability detection timing
  private var belowStopThresholdSince: Long = 0L
  private var stabilityStartTime: Long = 0L
  private var lastSensorEventTime: Long = 0L
  private var lastLogTime: Long = 0L
  private var sessionStartTime: Long = 0L

  private val mainHandler = Handler(Looper.getMainLooper())
  private val watchdogRunnable = object : Runnable {
    override fun run() {
      if (!isListening) return
      val now = System.currentTimeMillis()
      if (currentState == ShakeMotionState.ROLLING ||
          currentState == ShakeMotionState.MOTION_STOPPING ||
          currentState == ShakeMotionState.WAITING_FOR_STABILITY) {

        // If hardware sensor throttles/pauses when phone is stationary, decay motion
        if (now - lastSensorEventTime > 60L) {
          currentRawAcc *= 0.50f
          currentGyroEquivalent *= 0.50f
          instantMotion = if (isGyroSupported) {
            (currentRawAcc * 0.70f) + (currentGyroEquivalent * 0.30f)
          } else {
            currentRawAcc
          }
          filteredMotion = (filteredMotion * 0.50f) + (instantMotion * 0.50f)
        }

        evaluateMotionState(now)

        if (currentState == ShakeMotionState.ROLLING ||
            currentState == ShakeMotionState.MOTION_STOPPING ||
            currentState == ShakeMotionState.WAITING_FOR_STABILITY) {
          mainHandler.postDelayed(this, 35L)
        }
      }
    }
  }

  companion object {
    private const val TAG = "ShakeDice"
    // Short settling timer: 250–350ms of confirmed low motion
    private const val STOP_SETTLE_TIME_MS = 280L
    // Stability duration after roll before re-arming (500-800ms)
    private const val STABILITY_DURATION_MS = 600L
    // Safety cap for continuous shaking
    private const val MAX_SESSION_DURATION_MS = 30000L
  }

  fun start() {
    if (!isSupported || isListening) return
    isListening = true
    isGravityInitialized = false
    isPrevRawInitialized = false
    currentRawAcc = 0f
    currentGyroEquivalent = 0f
    instantMotion = 0f
    filteredMotion = 0f
    belowStopThresholdSince = 0L
    stabilityStartTime = 0L
    isRollSessionActive = false
    currentState = ShakeMotionState.ARMED
    Log.d(TAG, "STATE = ARMED")

    accelerometer?.let {
      sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
    gyroscope?.let {
      sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
  }

  fun stop() {
    if (!isListening) return
    isListening = false
    stopWatchdog()
    try {
      sensorManager?.unregisterListener(this)
    } catch (_: Exception) {
      // Graceful unregister
    }
    currentState = ShakeMotionState.IDLE
    Log.d(TAG, "STATE = IDLE")
    isRollSessionActive = false
    isGravityInitialized = false
    isPrevRawInitialized = false
    filteredMotion = 0f
    instantMotion = 0f
    belowStopThresholdSince = 0L
    stabilityStartTime = 0L
    notifyTelemetry(System.currentTimeMillis())
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || !isEnabled) return
    val now = System.currentTimeMillis()
    lastSensorEventTime = now

    when (event.sensor.type) {
      Sensor.TYPE_ACCELEROMETER -> {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!isGravityInitialized) {
          gravity[0] = x
          gravity[1] = y
          gravity[2] = z
          isGravityInitialized = true
          prevRawX = x
          prevRawY = y
          prevRawZ = z
          isPrevRawInitialized = true
          return
        }

        // Dynamic gravity tracking filter (isolates true stationary gravity orientation)
        val alpha = 0.82f
        gravity[0] = alpha * gravity[0] + (1f - alpha) * x
        gravity[1] = alpha * gravity[1] + (1f - alpha) * y
        gravity[2] = alpha * gravity[2] + (1f - alpha) * z

        // Pure linear acceleration with gravity subtracted (in m/s²)
        val lx = x - gravity[0]
        val ly = y - gravity[1]
        val lz = z - gravity[2]
        val linearAccMag = sqrt(lx * lx + ly * ly + lz * lz)

        // Sample-to-sample difference (jerk component)
        val dx = x - prevRawX
        val dy = y - prevRawY
        val dz = z - prevRawZ
        prevRawX = x
        prevRawY = y
        prevRawZ = z
        val jerkMag = sqrt(dx * dx + dy * dy + dz * dz)

        currentRawAcc = max(linearAccMag, jerkMag * 0.70f)
        processMotionEvaluation(now)
      }

      Sensor.TYPE_GYROSCOPE -> {
        val gx = event.values[0]
        val gy = event.values[1]
        val gz = event.values[2]
        val gyroNorm = sqrt(gx * gx + gy * gy + gz * gz)
        // At rest, gyroscope drift is typically < 0.08 rad/s
        val cleanGyro = if (gyroNorm > 0.08f) (gyroNorm - 0.08f) else 0f
        currentGyroEquivalent = cleanGyro * 1.5f
        processMotionEvaluation(now)
      }
    }
  }

  private fun processMotionEvaluation(now: Long) {
    instantMotion = if (isGyroSupported) {
      (currentRawAcc * 0.70f) + (currentGyroEquivalent * 0.30f)
    } else {
      currentRawAcc
    }

    // Exponential moving average filter:
    // alpha = 0.55f bridges zero-crossing reversals while decaying rapidly on stop
    filteredMotion = (filteredMotion * 0.55f) + (instantMotion * 0.45f)

    evaluateMotionState(now)
  }

  private fun evaluateMotionState(now: Long) {
    when (currentState) {
      ShakeMotionState.IDLE, ShakeMotionState.ARMED -> {
        // Only a genuinely new motion exceeding START_THRESHOLD can start a roll session
        if (!isRollSessionActive && filteredMotion >= sensitivity.motionStartThreshold) {
          isRollSessionActive = true
          rollSessionId++
          sessionStartTime = now
          belowStopThresholdSince = 0L

          currentState = ShakeMotionState.MOTION_STARTED
          Log.d(TAG, "STATE = MOTION_STARTED")
          Log.d(TAG, "rollSessionId = $rollSessionId")

          currentState = ShakeMotionState.ROLLING
          Log.d(TAG, "STATE = ROLLING")

          notifyTelemetry(now)
          startWatchdog()
          listener.onMotionStarted()
        } else {
          notifyTelemetry(now)
        }
      }

      ShakeMotionState.MOTION_STARTED -> {
        currentState = ShakeMotionState.ROLLING
        Log.d(TAG, "STATE = ROLLING")
        notifyTelemetry(now)
        startWatchdog()
        listener.onMotionStarted()
      }

      ShakeMotionState.ROLLING -> {
        // Continuous shaking in the SAME roll session
        if (filteredMotion >= sensitivity.motionContinueThreshold) {
          // Strong continuous motion: keep rolling
          belowStopThresholdSince = 0L
        } else if (filteredMotion < sensitivity.motionStopThreshold) {
          // Movement dropped below stop threshold: enter MOTION_STOPPING
          currentState = ShakeMotionState.MOTION_STOPPING
          Log.d(TAG, "STATE = MOTION_STOPPING")
          belowStopThresholdSince = now
        }

        logPeriodicDebug(now)
        notifyTelemetry(now)
      }

      ShakeMotionState.MOTION_STOPPING -> {
        // Check if movement resumed (e.g. turnaround/reversal in continuous shake)
        if (filteredMotion >= sensitivity.motionContinueThreshold) {
          // Continuous shake resumed! Return to ROLLING
          currentState = ShakeMotionState.ROLLING
          Log.d(TAG, "STATE = ROLLING")
          belowStopThresholdSince = 0L
        } else if (filteredMotion < sensitivity.motionStopThreshold) {
          val timeBelowStop = if (belowStopThresholdSince > 0L) (now - belowStopThresholdSince) else 0L
          if (timeBelowStop >= STOP_SETTLE_TIME_MS || (now - sessionStartTime >= MAX_SESSION_DURATION_MS)) {
            // Physical motion genuinely stopped: transition to SETTLING
            stopWatchdog()
            currentState = ShakeMotionState.SETTLING
            Log.d(TAG, "STATE = SETTLING")
            belowStopThresholdSince = 0L
            notifyTelemetry(now)
            listener.onMotionSettling()
            return
          }
        }

        logPeriodicDebug(now)
        notifyTelemetry(now)
      }

      ShakeMotionState.SETTLING,
      ShakeMotionState.MOTION_SETTLING,
      ShakeMotionState.FINAL_RESULT -> {
        // IGNORE all sensor fluctuations during settling and final result display
        notifyTelemetry(now)
      }

      ShakeMotionState.WAITING_FOR_STABILITY -> {
        // Phone must remain continuously below the stop threshold to re-arm
        if (filteredMotion < sensitivity.motionStopThreshold) {
          val timeStable = now - stabilityStartTime
          if (timeStable >= STABILITY_DURATION_MS) {
            // Phone is genuinely stable! Re-arm for next roll
            stopWatchdog()
            currentState = ShakeMotionState.ARMED
            isRollSessionActive = false
            Log.d(TAG, "STATE = ARMED")
            Log.d(TAG, "STATE = IDLE")
          }
        } else {
          // Phone is still moving (hand tremor or user still finishing motion): reset stability timer
          stabilityStartTime = now
        }

        logPeriodicDebug(now)
        notifyTelemetry(now)
      }

      ShakeMotionState.MOTION_DETECTED -> {
        currentState = ShakeMotionState.ROLLING
        Log.d(TAG, "STATE = ROLLING")
        notifyTelemetry(now)
        startWatchdog()
        listener.onMotionStarted()
      }
    }
  }

  private fun logPeriodicDebug(now: Long) {
    if (now - lastLogTime >= 200L) {
      lastLogTime = now
      val timeStable = if (currentState == ShakeMotionState.WAITING_FOR_STABILITY) (now - stabilityStartTime) else 0L
      val timeBelow = if (belowStopThresholdSince > 0L) (now - belowStopThresholdSince) else 0L
      Log.d(
        TAG,
        "rollSessionId: $rollSessionId | " +
        "motionIntensity: ${String.format(Locale.US, "%.2f", instantMotion)} | " +
        "filteredMotion: ${String.format(Locale.US, "%.2f", filteredMotion)} | " +
        "currentState: $currentState | " +
        "timeStable: ${timeStable}ms | " +
        "timeBelowStop: ${timeBelow}ms"
      )
    }
  }

  private fun startWatchdog() {
    mainHandler.removeCallbacks(watchdogRunnable)
    mainHandler.postDelayed(watchdogRunnable, 35L)
  }

  private fun stopWatchdog() {
    mainHandler.removeCallbacks(watchdogRunnable)
  }

  /**
   * Called by the ViewModel when the final result has finished settling.
   * Enters WAITING_FOR_STABILITY before re-arming.
   */
  fun notifyFinalResult() {
    currentState = ShakeMotionState.FINAL_RESULT
    Log.d(TAG, "STATE = FINAL_RESULT")
    val now = System.currentTimeMillis()
    notifyTelemetry(now)

    // Transition to WAITING_FOR_STABILITY to prevent duplicate rolls from the same shake
    currentState = ShakeMotionState.WAITING_FOR_STABILITY
    Log.d(TAG, "STATE = WAITING_FOR_STABILITY")
    stabilityStartTime = now
    belowStopThresholdSince = 0L
    startWatchdog()
    notifyTelemetry(now)
  }

  private fun notifyTelemetry(now: Long) {
    val timeBelow = if (belowStopThresholdSince > 0L) (now - belowStopThresholdSince) else 0L
    val timeStable = if (currentState == ShakeMotionState.WAITING_FOR_STABILITY) (now - stabilityStartTime) else 0L
    listener.onTelemetryUpdate(
      MotionDebugInfo(
        state = currentState,
        acceleration = currentRawAcc,
        motionIntensity = instantMotion,
        filteredMotion = filteredMotion,
        timeBelowThresholdMs = timeBelow,
        timeStableMs = timeStable,
        rollSessionId = rollSessionId,
        isGyroAvailable = isGyroSupported
      )
    )
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op for inertial motion sensing
  }
}

