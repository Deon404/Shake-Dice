package com.example.sensors

/**
 * Robust physical motion state machine for real physical dice rolls:
 * IDLE / ARMED -> MOTION_STARTED -> ROLLING -> MOTION_STOPPING -> SETTLING -> FINAL_RESULT -> WAITING_FOR_STABILITY -> IDLE / ARMED
 */
enum class ShakeMotionState {
  IDLE,
  ARMED,
  MOTION_STARTED,
  MOTION_DETECTED,
  ROLLING,
  MOTION_STOPPING,
  SETTLING,
  MOTION_SETTLING,
  FINAL_RESULT,
  WAITING_FOR_STABILITY
}

/**
 * Real-time motion telemetry for development and physical feel verification.
 */
data class MotionDebugInfo(
  val state: ShakeMotionState = ShakeMotionState.IDLE,
  val acceleration: Float = 0f,
  val motionIntensity: Float = 0f,
  val filteredMotion: Float = 0f,
  val timeBelowThresholdMs: Long = 0L,
  val timeStableMs: Long = 0L,
  val rollSessionId: Long = 0L,
  val isGyroAvailable: Boolean = false
)
