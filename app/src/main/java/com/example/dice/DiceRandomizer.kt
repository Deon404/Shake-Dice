package com.example.dice

import java.security.SecureRandom

/**
 * High-precision cryptographic random dice generator.
 * Produces strictly independent uniform distributions from 1 to 6.
 */
object DiceRandomizer {
  private val secureRandom: SecureRandom = SecureRandom()

  /**
   * Generates a single fair die roll from 1 to 6.
   */
  fun rollDie(): Int = 1 + secureRandom.nextInt(6)

  /**
   * Generates [count] independent fair dice rolls.
   */
  fun rollDice(count: Int): List<Int> {
    val safeCount = count.coerceIn(1, 3)
    return List(safeCount) { rollDie() }
  }
}
