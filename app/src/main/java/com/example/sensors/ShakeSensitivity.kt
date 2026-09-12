package com.example.sensors

enum class ShakeSensitivity(
  val displayName: String,
  val thresholdG: Float,
  val description: String
) {
  LOW(
    displayName = "Low",
    thresholdG = 1.05f,
    description = "Requires a firm, deliberate shake"
  ),
  MEDIUM(
    displayName = "Medium",
    thresholdG = 0.65f,
    description = "Standard comfortable shake"
  ),
  HIGH(
    displayName = "High",
    thresholdG = 0.40f,
    description = "More sensitive to lighter motions"
  );

  companion object {
    val DEFAULT = MEDIUM
  }
}
