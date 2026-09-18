package com.aivigil.day0check

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompassViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = SensorDataManager(application)
    private val _uiState = MutableStateFlow<CompassUiState>(
        if (sensorManager.hasRequiredSensors) CompassUiState.Content() else CompassUiState.NoSensor
    )
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    init {
        if (sensorManager.hasRequiredSensors) {
            startListening()
        }
    }

    fun calibrateZero() {
        sensorManager.calibrateZero()
    }

    fun resetCalibration() {
        sensorManager.resetCalibration()
    }

    private fun startListening() {
        viewModelScope.launch {
            sensorManager.getSensorStream().collect { snapshot ->
                val isUnreliable = snapshot.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                                   snapshot.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW

                _uiState.value = CompassUiState.Content(
                    headingDegrees = snapshot.heading,
                    pitchDegrees = snapshot.pitch,
                    rollDegrees = snapshot.roll,
                    isLevel = snapshot.isLevel,
                    accuracy = snapshot.accuracy,
                    isUnreliable = isUnreliable,
                    isCompassAvailable = snapshot.isCompassAvailable,
                    isCalibrated = snapshot.isCalibrated
                )
            }
        }
    }
}
