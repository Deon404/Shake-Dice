package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryRepository
import com.example.data.RollRecord
import com.example.data.SettingsRepository
import com.example.dice.DiceRandomizer
import com.example.haptics.HapticManager
import com.example.sensors.MotionDebugInfo
import com.example.sensors.ShakeDetector
import com.example.sensors.ShakeMotionListener
import com.example.sensors.ShakeSensitivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class DiceUiState(
  val diceValues: List<Int> = listOf(4, 3),
  val diceCount: Int = 2,
  val isRolling: Boolean = false,
  val isShakeToRollEnabled: Boolean = true,
  val isHapticsEnabled: Boolean = true,
  val shakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
  val isSensorSupported: Boolean = true,
  val isHapticsSupported: Boolean = true,
  val history: List<RollRecord> = emptyList(),
  val rotationAngles: List<Float> = listOf(0f, 0f, 0f),
  val scaleFactors: List<Float> = listOf(1f, 1f, 1f),
  val verticalOffsets: List<Float> = listOf(0f, 0f, 0f),
  val debugInfo: MotionDebugInfo = MotionDebugInfo()
) {
  val totalSum: Int = diceValues.sum()
}

class DiceViewModel(application: Application) : AndroidViewModel(application), ShakeMotionListener {

  private val settingsRepo = SettingsRepository(application)
  private val historyRepo = HistoryRepository(application)
  val hapticManager = HapticManager(application)

  private val _uiState = MutableStateFlow(
    DiceUiState(
      diceCount = settingsRepo.diceCount,
      diceValues = DiceRandomizer.rollDice(settingsRepo.diceCount),
      isShakeToRollEnabled = settingsRepo.isShakeToRollEnabled,
      isHapticsEnabled = settingsRepo.isHapticsEnabled,
      shakeSensitivity = settingsRepo.shakeSensitivity,
      isHapticsSupported = hapticManager.isAvailable,
      history = historyRepo.loadHistory()
    )
  )
  val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

  val shakeDetector = ShakeDetector(application, this).apply {
    isEnabled = settingsRepo.isShakeToRollEnabled
    sensitivity = settingsRepo.shakeSensitivity
  }

  init {
    hapticManager.isEnabled = settingsRepo.isHapticsEnabled
    _uiState.update { it.copy(isSensorSupported = shakeDetector.isSupported) }
  }

  private var rollJob: Job? = null
  @Volatile private var isContinuousRolling = false
  @Volatile private var isManualRolling = false
  @Volatile private var isSettling = false

  // -------------------------------------------------------------
  // CONTINUOUS SHAKE MOTION LISTENER IMPLEMENTATION
  // -------------------------------------------------------------

  override fun onMotionStarted() {
    viewModelScope.launch {
      if (!_uiState.value.isShakeToRollEnabled) return@launch
      if (isManualRolling || isContinuousRolling || isSettling) return@launch

      isContinuousRolling = true
      _uiState.update { it.copy(isRolling = true) }

      // Initial physical pickup haptic
      hapticManager.performShakeDetectedHaptic()

      rollJob?.cancel()
      rollJob = viewModelScope.launch {
        var loopCount = 0
        // CONTINUOUS TUMBLING LOOP
        // As long as physical movement continues, the dice keep rolling!
        while (isContinuousRolling) {
          val currentCount = _uiState.value.diceCount
          val tempFaces = List(currentCount) { 1 + Random.nextInt(6) }
          val rotations = List(currentCount) { (Random.nextFloat() * 40f) - 20f }
          val scales = List(currentCount) { 0.92f + (Random.nextFloat() * 0.14f) }
          val offsets = List(currentCount) { (Random.nextFloat() * -18f) + 2f }

          _uiState.update {
            it.copy(
              diceValues = tempFaces,
              rotationAngles = rotations,
              scaleFactors = scales,
              verticalOffsets = offsets
            )
          }

          // Subtle intermittent haptic cadence during tumbling (cup rattling feel)
          loopCount++
          if (loopCount % 4 == 0) { // ~160ms intervals
            hapticManager.performContinuousRollingTick()
          }

          delay(40L)
        }

        // Motion has settled: transition into natural landing sequence
        performSettlingSequence(_uiState.value.diceCount)
      }
    }
  }

  override fun onMotionSettling() {
    // Flag to break out of while(isContinuousRolling) tumbling loop
    if (!isContinuousRolling) return
    isContinuousRolling = false
  }

  override fun onTelemetryUpdate(debugInfo: MotionDebugInfo) {
    _uiState.update { it.copy(debugInfo = debugInfo) }
  }

  /**
   * Natural physical settling animation:
   * Fast movement -> slower movement -> small bounce -> final face resting
   * Total duration: ~280ms (within target 250-450ms)
   */
  private suspend fun performSettlingSequence(count: Int) {
    isSettling = true
    try {
      // Settling step 1 (deceleration)
      _uiState.update {
        it.copy(
          diceValues = List(count) { 1 + Random.nextInt(6) },
          rotationAngles = List(count) { (Random.nextFloat() * 18f) - 9f },
          scaleFactors = List(count) { 0.96f + (Random.nextFloat() * 0.05f) },
          verticalOffsets = List(count) { (Random.nextFloat() * -8f) + 1f }
        )
      }
      delay(65L)

      // Settling step 2 (subtle bounce)
      _uiState.update {
        it.copy(
          diceValues = List(count) { 1 + Random.nextInt(6) },
          rotationAngles = List(count) { (Random.nextFloat() * 8f) - 4f },
          scaleFactors = List(count) { 0.98f + (Random.nextFloat() * 0.03f) },
          verticalOffsets = List(count) { (Random.nextFloat() * -4f) }
        )
      }
      delay(95L)

      // Settling step 3 (final rest alignment)
      _uiState.update {
        it.copy(
          diceValues = List(count) { 1 + Random.nextInt(6) },
          rotationAngles = List(count) { (Random.nextFloat() * 3f) - 1.5f },
          scaleFactors = List(count) { 1.005f },
          verticalOffsets = List(count) { -1f }
        )
      }
      delay(120L)

      // TRUE RANDOM RESULT generated via secure DiceRandomizer
      val finalResult = DiceRandomizer.rollDice(count)
      val zeroRotations = List(count) { 0f }
      val standardScales = List(count) { 1f }
      val zeroOffsets = List(count) { 0f }

      // Log ShakeDice: FINAL RESULT and complete state transition
      shakeDetector.notifyFinalResult()

      // Single history entry recorded for this continuous shake session
      val newRecord = RollRecord(
        diceValues = finalResult,
        isShakeTriggered = true
      )
      val updatedHistory = listOf(newRecord) + _uiState.value.history.take(HistoryRepository.MAX_HISTORY_ENTRIES - 1)
      historyRepo.saveHistory(updatedHistory)

      _uiState.update {
        it.copy(
          diceValues = finalResult,
          rotationAngles = zeroRotations,
          scaleFactors = standardScales,
          verticalOffsets = zeroOffsets,
          isRolling = false,
          history = updatedHistory
        )
      }

      // Confirmation landing haptic
      hapticManager.performDiceLandingHaptic()
    } finally {
      isSettling = false
      isContinuousRolling = false
    }
  }

  // -------------------------------------------------------------
  // MANUAL BUTTON ROLL (Standard fixed-duration roll)
  // -------------------------------------------------------------

  fun rollDice(isFromShake: Boolean = false) {
    if (_uiState.value.isRolling || isSettling) return

    isManualRolling = true
    hapticManager.performManualRollHaptic()

    rollJob?.cancel()
    rollJob = viewModelScope.launch {
      _uiState.update { it.copy(isRolling = true) }

      val currentCount = _uiState.value.diceCount
      val animationSteps = 10
      val stepDurationMs = 55L

      for (i in 0 until animationSteps) {
        val tempFaces = List(currentCount) { 1 + Random.nextInt(6) }
        val rotations = List(currentCount) { (Random.nextFloat() * 26f) - 13f }
        val scales = List(currentCount) { 0.94f + (Random.nextFloat() * 0.11f) }
        val offsets = List(currentCount) { (Random.nextFloat() * -14f) + 2f }

        _uiState.update {
          it.copy(
            diceValues = tempFaces,
            rotationAngles = rotations,
            scaleFactors = scales,
            verticalOffsets = offsets
          )
        }
        delay(stepDurationMs)
      }

      val finalResult = DiceRandomizer.rollDice(currentCount)
      val zeroRotations = List(currentCount) { 0f }
      val standardScales = List(currentCount) { 1f }
      val zeroOffsets = List(currentCount) { 0f }

      val newRecord = RollRecord(
        diceValues = finalResult,
        isShakeTriggered = false
      )

      val updatedHistory = listOf(newRecord) + _uiState.value.history.take(HistoryRepository.MAX_HISTORY_ENTRIES - 1)
      historyRepo.saveHistory(updatedHistory)

      _uiState.update {
        it.copy(
          diceValues = finalResult,
          rotationAngles = zeroRotations,
          scaleFactors = standardScales,
          verticalOffsets = zeroOffsets,
          isRolling = false,
          history = updatedHistory
        )
      }

      hapticManager.performDiceLandingHaptic()
      isManualRolling = false
    }
  }

  fun setDiceCount(count: Int) {
    if (_uiState.value.isRolling) return
    val safeCount = count.coerceIn(1, 3)
    settingsRepo.diceCount = safeCount

    val currentValues = _uiState.value.diceValues
    val newValues = when {
      currentValues.size == safeCount -> currentValues
      currentValues.size < safeCount -> currentValues + DiceRandomizer.rollDice(safeCount - currentValues.size)
      else -> currentValues.take(safeCount)
    }

    _uiState.update {
      it.copy(
        diceCount = safeCount,
        diceValues = newValues,
        rotationAngles = List(safeCount) { 0f },
        scaleFactors = List(safeCount) { 1f },
        verticalOffsets = List(safeCount) { 0f }
      )
    }
  }

  fun toggleShake(enabled: Boolean) {
    settingsRepo.isShakeToRollEnabled = enabled
    shakeDetector.isEnabled = enabled
    _uiState.update { it.copy(isShakeToRollEnabled = enabled) }
  }

  fun toggleHaptics(enabled: Boolean) {
    settingsRepo.isHapticsEnabled = enabled
    hapticManager.isEnabled = enabled
    _uiState.update { it.copy(isHapticsEnabled = enabled) }
  }

  fun setShakeSensitivity(sensitivity: ShakeSensitivity) {
    settingsRepo.shakeSensitivity = sensitivity
    shakeDetector.sensitivity = sensitivity
    _uiState.update { it.copy(shakeSensitivity = sensitivity) }
  }

  fun clearHistory() {
    historyRepo.clearHistory()
    _uiState.update { it.copy(history = emptyList()) }
  }

  override fun onCleared() {
    super.onCleared()
    shakeDetector.stop()
  }
}
