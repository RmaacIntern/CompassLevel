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
    val isReliable: Boolean = true,
    val isTrueNorth: Boolean = false,
    val declination: Float = 0f,
    val usePercentGrade: Boolean = false
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

    // Last known good heading for "Heading Hold" buffer when accuracy drops to UNRELIABLE
    private var lastKnownGoodHeading = 0f

    // Settings
    private var isTrueNorth = false
    private var declination = 0f
    private var usePercentGrade = false

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

    fun setTrueNorth(enabled: Boolean, manualDeclination: Float = 0f) {
        isTrueNorth = enabled
        declination = manualDeclination
        _compassState.value = _compassState.value.copy(
            isTrueNorth = enabled,
            declination = manualDeclination
        )
    }

    fun setUsePercentGrade(enabled: Boolean) {
        usePercentGrade = enabled
        _compassState.value = _compassState.value.copy(usePercentGrade = enabled)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            var targetAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (targetAzimuth < 0) targetAzimuth += 360f

            // Apply declination if True North enabled
            if (isTrueNorth) {
                targetAzimuth = (targetAzimuth + declination + 360f) % 360f
            }

            rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            // Heading Hold Buffer: If accuracy is UNRELIABLE, hold last known good heading
            val targetHeading = if (_isReliable.value) {
                // Shortest-angular-delta wrapping to eliminate 359° - 0° snap spin
                val currentHeading = _headingFlow.value
                val delta = ((targetAzimuth - currentHeading + 540f) % 360f) - 180f
                var smoothed = currentHeading + alpha * delta
                if (smoothed < 0f) smoothed += 360f
                if (smoothed >= 360f) smoothed -= 360f
                lastKnownGoodHeading = smoothed
                smoothed
            } else {
                lastKnownGoodHeading
            }

            val compensatedPitch = rawPitch - tarePitch
            val compensatedRoll = rawRoll - tareRoll

            val smoothedPitch = _pitchFlow.value + alpha * (compensatedPitch - _pitchFlow.value)
            val smoothedRoll = _rollFlow.value + alpha * (compensatedRoll - _rollFlow.value)

            _headingFlow.value = targetHeading
            _pitchFlow.value = smoothedPitch
            _rollFlow.value = smoothedRoll

            val isLevel = abs(smoothedPitch) <= 0.5f && abs(smoothedRoll) <= 0.5f
            _compassState.value = CompassState(
                heading = targetHeading,
                pitch = smoothedPitch,
                roll = smoothedRoll,
                isLevel = isLevel,
                isReliable = _isReliable.value,
                isTrueNorth = isTrueNorth,
                declination = declination,
                usePercentGrade = usePercentGrade
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
                isReliable = _isReliable.value,
                isTrueNorth = isTrueNorth,
                declination = declination,
                usePercentGrade = usePercentGrade
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val reliable = (accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE)
        _isReliable.value = reliable
        _compassState.value = _compassState.value.copy(isReliable = reliable)
    }
}
