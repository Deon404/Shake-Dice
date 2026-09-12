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
  fun `shake sensitivity hysteresis thresholds satisfy start greater than continue greater than stop`() {
    ShakeSensitivity.values().forEach { sensitivity ->
      assertTrue(
        "${sensitivity.name}: startThreshold must exceed continueThreshold",
        sensitivity.motionStartThreshold > sensitivity.motionContinueThreshold
      )
      assertTrue(
        "${sensitivity.name}: continueThreshold must exceed stopThreshold",
        sensitivity.motionContinueThreshold > sensitivity.motionStopThreshold
      )
    }

    // Explicitly verify default MEDIUM sensitivity matches the user target ranges
    val medium = ShakeSensitivity.MEDIUM
    assertTrue("Medium start threshold should be in 2.5-3.0 range", medium.motionStartThreshold in 2.5f..3.0f)
    assertTrue("Medium continue threshold should be in 1.4-2.0 range", medium.motionContinueThreshold in 1.4f..2.0f)
    assertTrue("Medium stop threshold should be in 0.5-1.0 range", medium.motionStopThreshold in 0.5f..1.0f)
  }

  @Test
  fun `shake motion state machine lifecycle covers all required continuous phases`() {
    val states = com.example.sensors.ShakeMotionState.values().map { it.name }
    assertTrue(states.contains("IDLE"))
    assertTrue(states.contains("ARMED"))
    assertTrue(states.contains("MOTION_STARTED"))
    assertTrue(states.contains("ROLLING"))
    assertTrue(states.contains("MOTION_STOPPING"))
    assertTrue(states.contains("SETTLING"))
    assertTrue(states.contains("FINAL_RESULT"))
    assertTrue(states.contains("WAITING_FOR_STABILITY"))
  }

  @Test
  fun `die themes match red white black sequence for indices 0 1 2`() {
    assertEquals(com.example.dice.DieTheme.CRIMSON_RED, com.example.dice.DieTheme.forIndex(0))
    assertEquals(com.example.dice.DieTheme.PORCELAIN_WHITE, com.example.dice.DieTheme.forIndex(1))
    assertEquals(com.example.dice.DieTheme.OBSIDIAN_BLACK, com.example.dice.DieTheme.forIndex(2))
  }
}

