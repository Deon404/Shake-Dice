package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryRepository
import com.example.data.RollRecord
import com.example.data.SettingsRepository
import com.example.dice.DiceRandomizer
import com.example.haptics.HapticManager
import com.example.sensors.ShakeDetector
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
  val verticalOffsets: List<Float> = listOf(0f, 0f, 0f)
) {
  val totalSum: Int = diceValues.sum()
}

class DiceViewModel(application: Application) : AndroidViewModel(application) {

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

  val shakeDetector = ShakeDetector(application) {
    // Shaken callback from sensor thread dispatched to Main/UI thread safely
    viewModelScope.launch {
      triggerShakeRoll()
    }
  }.apply {
    isEnabled = settingsRepo.isShakeToRollEnabled
    sensitivity = settingsRepo.shakeSensitivity
  }

  init {
    hapticManager.isEnabled = settingsRepo.isHapticsEnabled
    _uiState.update { it.copy(isSensorSupported = shakeDetector.isSupported) }
  }

  private var rollJob: Job? = null

  fun triggerShakeRoll() {
    if (_uiState.value.isRolling || !_uiState.value.isShakeToRollEnabled) return
    rollDice(isFromShake = true)
  }

  fun rollDice(isFromShake: Boolean = false) {
    if (_uiState.value.isRolling) return

    if (isFromShake) {
      hapticManager.performShakeDetectedHaptic()
    } else {
      hapticManager.performManualRollHaptic()
    }

    rollJob?.cancel()
    rollJob = viewModelScope.launch {
      _uiState.update { it.copy(isRolling = true) }

      val currentCount = _uiState.value.diceCount
      val animationSteps = 10
      val stepDurationMs = 55L

      // Intermediate animation tumbling loop
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

      // Final True Random Cryptographic Result
      val finalResult = DiceRandomizer.rollDice(currentCount)
      val zeroRotations = List(currentCount) { 0f }
      val standardScales = List(currentCount) { 1f }
      val zeroOffsets = List(currentCount) { 0f }

      val newRecord = RollRecord(
        diceValues = finalResult,
        isShakeTriggered = isFromShake
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

      // Landing confirmation haptic
      hapticManager.performDiceLandingHaptic()
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
