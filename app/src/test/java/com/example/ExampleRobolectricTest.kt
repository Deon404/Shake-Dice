package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RollRecord
import com.example.dice.DiceRandomizer
import com.example.dice.PipPosition
import com.example.sensors.ShakeSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shake Dice", appName)
  }

  @Test
  fun `dice randomizer produces valid independent values between 1 and 6`() {
    for (count in 1..3) {
      val dice = DiceRandomizer.rollDice(count)
      assertEquals(count, dice.size)
      dice.forEach { value ->
        assertTrue("Die value $value must be between 1 and 6", value in 1..6)
      }
    }
  }

  @Test
  fun `pip positions correctly map for standard dice 1 through 6`() {
    assertEquals(1, PipPosition.forValue(1).size)
    assertEquals(2, PipPosition.forValue(2).size)
    assertEquals(3, PipPosition.forValue(3).size)
    assertEquals(4, PipPosition.forValue(4).size)
    assertEquals(5, PipPosition.forValue(5).size)
    assertEquals(6, PipPosition.forValue(6).size)

    assertTrue(PipPosition.forValue(1).contains(PipPosition.CENTER))
    assertTrue(PipPosition.forValue(5).contains(PipPosition.CENTER))
  }

  @Test
  fun `roll record formats formula and total properly`() {
    val singleDie = RollRecord(diceValues = listOf(6))
    assertEquals(6, singleDie.total)
    assertEquals("6", singleDie.formattedFormula)

    val twoDice = RollRecord(diceValues = listOf(4, 3))
    assertEquals(7, twoDice.total)
    assertEquals("4 + 3 = 7", twoDice.formattedFormula)

    val threeDice = RollRecord(diceValues = listOf(2, 5, 1))
    assertEquals(8, threeDice.total)
    assertEquals("2 + 5 + 1 = 8", threeDice.formattedFormula)
  }

  @Test
  fun `shake sensitivity thresholds are properly ordered`() {
    assertTrue(ShakeSensitivity.LOW.thresholdG > ShakeSensitivity.MEDIUM.thresholdG)
    assertTrue(ShakeSensitivity.MEDIUM.thresholdG > ShakeSensitivity.HIGH.thresholdG)
  }

  @Test
  fun `die themes match red white black sequence for indices 0 1 2`() {
    assertEquals(com.example.dice.DieTheme.CRIMSON_RED, com.example.dice.DieTheme.forIndex(0))
    assertEquals(com.example.dice.DieTheme.PORCELAIN_WHITE, com.example.dice.DieTheme.forIndex(1))
    assertEquals(com.example.dice.DieTheme.OBSIDIAN_BLACK, com.example.dice.DieTheme.forIndex(2))
  }
}

