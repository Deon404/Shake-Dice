package com.example.sensors

enum class ShakeSensitivity(
  val displayName: String,
  val thresholdG: Float,
  val motionStartThreshold: Float,
  val motionContinueThreshold: Float,
  val motionStopThreshold: Float,
  val description: String
) {
  LOW(
    displayName = "Low",
    thresholdG = 0.35f,
    motionStartThreshold = 3.5f,
    motionContinueThreshold = 2.0f,
    motionStopThreshold = 0.90f,
    description = "Requires a firm, deliberate shake"
  ),
  MEDIUM(
    displayName = "Medium",
    thresholdG = 0.27f,
    motionStartThreshold = 2.7f,
    motionContinueThreshold = 1.5f,
    motionStopThreshold = 0.70f,
    description = "Standard comfortable shake"
  ),
  HIGH(
    displayName = "High",
    thresholdG = 0.19f,
    motionStartThreshold = 1.9f,
    motionContinueThreshold = 1.1f,
    motionStopThreshold = 0.50f,
    description = "More sensitive to lighter motions"
  );

  companion object {
    val DEFAULT = MEDIUM
  }
}
