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
import kotlin.math.atan2
import kotlin.math.sqrt

data class CompassState(
    val heading: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isLevel: Boolean = false,
    val isReliable: Boolean = true
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasMagnetometer: Boolean = (rotationVectorSensor != null) || (magnetometer != null)

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    // Backwards-compatible individual flows
    val headingFlow: StateFlow<Float> get() = _headingFlow
    private val _headingFlow = MutableStateFlow(0f)

    val pitchFlow: StateFlow<Float> get() = _pitchFlow
    private val _pitchFlow = MutableStateFlow(0f)

    val rollFlow: StateFlow<Float> get() = _rollFlow
    private val _rollFlow = MutableStateFlow(0f)

    val isReliable: StateFlow<Boolean> get() = _isReliable
    private val _isReliable = MutableStateFlow(true)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Tare offsets for camera bump compensation
    private var tarePitch = 0f
    private var tareRoll = 0f
    private var rawPitch = 0f
    private var rawRoll = 0f

    // Smoothing factor (EMA alpha): 0.18f provides snappy 60fps response with zero jitter
    private val alpha = 0.18f

    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun tare() {
        tarePitch = rawPitch
        tareRoll = rawRoll
    }

    fun resetTare() {
        tarePitch = 0f
        tareRoll = 0f
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            var targetAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (targetAzimuth < 0) targetAzimuth += 360f

            rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            // Shortest-angular-delta wrapping to eliminate 359° - 0° snap spin
            val currentHeading = _headingFlow.value
            val delta = ((targetAzimuth - currentHeading + 540f) % 360f) - 180f
            var smoothedHeading = currentHeading + alpha * delta
            if (smoothedHeading < 0f) smoothedHeading += 360f
            if (smoothedHeading >= 360f) smoothedHeading -= 360f

            val compensatedPitch = rawPitch - tarePitch
            val compensatedRoll = rawRoll - tareRoll

            val smoothedPitch = _pitchFlow.value + alpha * (compensatedPitch - _pitchFlow.value)
            val smoothedRoll = _rollFlow.value + alpha * (compensatedRoll - _rollFlow.value)

            _headingFlow.value = smoothedHeading
            _pitchFlow.value = smoothedPitch
            _rollFlow.value = smoothedRoll

            val isLevel = abs(smoothedPitch) <= 0.5f && abs(smoothedRoll) <= 0.5f
            _compassState.value = CompassState(
                heading = smoothedHeading,
                pitch = smoothedPitch,
                roll = smoothedRoll,
                isLevel = isLevel,
                isReliable = _isReliable.value
            )
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            rawPitch = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
            rawRoll = Math.toDegrees(atan2(-ax.toDouble(), az.toDouble())).toFloat()

            val compensatedPitch = rawPitch - tarePitch
            val compensatedRoll = rawRoll - tareRoll

            val smoothedPitch = _pitchFlow.value + alpha * (compensatedPitch - _pitchFlow.value)
            val smoothedRoll = _rollFlow.value + alpha * (compensatedRoll - _rollFlow.value)

            _pitchFlow.value = smoothedPitch
            _rollFlow.value = smoothedRoll

            val isLevel = abs(smoothedPitch) <= 0.5f && abs(smoothedRoll) <= 0.5f
            _compassState.value = CompassState(
                heading = _headingFlow.value,
                pitch = smoothedPitch,
                roll = smoothedRoll,
                isLevel = isLevel,
                isReliable = _isReliable.value
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val reliable = (accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE)
        _isReliable.value = reliable
        _compassState.value = _compassState.value.copy(isReliable = reliable)
    }
}
