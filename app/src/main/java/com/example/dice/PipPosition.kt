package com.example.dice

/**
 * Reusable mathematical 3x3 pip position system for authentic dice faces.
 *
 * (0,0) TOP_LEFT      (0,1) TOP_CENTER      (0,2) TOP_RIGHT
 * (1,0) CENTER_LEFT   (1,1) CENTER          (1,2) CENTER_RIGHT
 * (2,0) BOTTOM_LEFT   (2,1) BOTTOM_CENTER   (2,2) BOTTOM_RIGHT
 */
enum class PipPosition(val row: Int, val col: Int) {
  TOP_LEFT(0, 0),
  TOP_CENTER(0, 1),
  TOP_RIGHT(0, 2),
  CENTER_LEFT(1, 0),
  CENTER(1, 1),
  CENTER_RIGHT(1, 2),
  BOTTOM_LEFT(2, 0),
  BOTTOM_CENTER(2, 1),
  BOTTOM_RIGHT(2, 2);

  companion object {
    /**
     * Maps standard dice numbers 1 through 6 to their geometric pip positions.
     */
    fun forValue(value: Int): Set<PipPosition> = when (value.coerceIn(1, 6)) {
      1 -> setOf(CENTER)
      2 -> setOf(TOP_LEFT, BOTTOM_RIGHT)
      3 -> setOf(TOP_LEFT, CENTER, BOTTOM_RIGHT)
      4 -> setOf(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)
      5 -> setOf(TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT)
      6 -> setOf(TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT, TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT)
      else -> setOf(CENTER)
    }
  }
}
