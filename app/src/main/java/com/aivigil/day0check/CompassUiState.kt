package com.aivigil.day0check

sealed interface CompassUiState {
data object NoSensor : CompassUiState

data class Content(
    val headingDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val isLevel: Boolean = false,
    val accuracy: Int = 3,
    val isUnreliable: Boolean = false,
    val isCompassAvailable: Boolean = true
) : CompassUiState

data class Settings(
    val dampingFactor: Float = 0.15f,
    val hapticsEnabled: Boolean = true
) : CompassUiState
}
