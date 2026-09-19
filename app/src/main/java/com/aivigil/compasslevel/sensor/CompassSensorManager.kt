package com.aivigil.compasslevel.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class ScreenState {
    LOADING,
    CONTENT,
    LEVEL_ONLY,
    UNRELIABLE
}

data class CompassUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val heading: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isZeroed: Boolean = false,
    val zeroPitchOffset: Float = 0f,
    val zeroRollOffset: Float = 0f
)

class CompassSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private var smoothedHeading = 0f
    private var smoothedPitch = 0f
    private var smoothedRoll = 0f
    private var lastReliableHeading = 0f
    private val filterFactor = 0.15f

    fun startListening() {
        if (rotationVectorSensor == null && accelerometerSensor == null) {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.LEVEL_ONLY)
            return
        }

        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                _uiState.value = _uiState.value.copy(screenState = ScreenState.LEVEL_ONLY)
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun tapToZero() {
        val current = _uiState.value
        _uiState.value = current.copy(
            zeroPitchOffset = smoothedPitch,
            zeroRollOffset = smoothedRoll,
            isZeroed = true
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                var rawAzimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                if (rawAzimuth < 0) rawAzimuth += 360f

                val rawPitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

                smoothedHeading = smoothAngle(smoothedHeading, rawAzimuth, filterFactor)
                smoothedPitch += filterFactor * (rawPitch - smoothedPitch)
                smoothedRoll += filterFactor * (rawRoll - smoothedRoll)

                val currentState = _uiState.value
                val adjustedPitch = smoothedPitch - currentState.zeroPitchOffset
                val adjustedRoll = smoothedRoll - currentState.zeroRollOffset

                if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                    _uiState.value = currentState.copy(
                        screenState = ScreenState.UNRELIABLE,
                        heading = lastReliableHeading,
                        pitch = adjustedPitch,
                        roll = adjustedRoll
                    )
                } else {
                    lastReliableHeading = smoothedHeading
                    _uiState.value = currentState.copy(
                        screenState = ScreenState.CONTENT,
                        heading = smoothedHeading,
                        pitch = adjustedPitch,
                        roll = adjustedRoll
                    )
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                val rawPitch = Math.toDegrees(kotlin.math.atan2(ay.toDouble(), az.toDouble())).toFloat()
                val rawRoll = Math.toDegrees(kotlin.math.atan2(-ax.toDouble(), kotlin.math.sqrt((ay * ay + az * az).toDouble()))).toFloat()

                smoothedPitch += filterFactor * (rawPitch - smoothedPitch)
                smoothedRoll += filterFactor * (rawRoll - smoothedRoll)

                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    screenState = ScreenState.LEVEL_ONLY,
                    pitch = smoothedPitch - currentState.zeroPitchOffset,
                    roll = smoothedRoll - currentState.zeroRollOffset
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE && _uiState.value.screenState == ScreenState.CONTENT) {
            _uiState.value = _uiState.value.copy(
                screenState = ScreenState.UNRELIABLE,
                heading = lastReliableHeading
            )
        }
    }

    private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (current + diff * factor + 360f) % 360f
    }
}
