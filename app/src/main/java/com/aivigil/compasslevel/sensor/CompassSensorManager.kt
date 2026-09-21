package com.aivigil.compasslevel.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

class CompassSensorManager(context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "CompassSensor"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasMagnetometer: Boolean = (rotationVectorSensor != null) || (magnetometer != null)

    private val _headingFlow = MutableStateFlow(0f)
    val headingFlow: StateFlow<Float> = _headingFlow.asStateFlow()

    private val _pitchFlow = MutableStateFlow(0f)
    val pitchFlow: StateFlow<Float> = _pitchFlow.asStateFlow()

    private val _rollFlow = MutableStateFlow(0f)
    val rollFlow: StateFlow<Float> = _rollFlow.asStateFlow()

    private val _isReliable = MutableStateFlow(true)
    val isReliable: StateFlow<Boolean> = _isReliable.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastLogTime = 0L

    fun startListening() {
        Log.d(TAG, "startListening() called. hasMagnetometer=$hasMagnetometer")
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stopListening() {
        Log.d(TAG, "stopListening() called.")
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f

            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            _headingFlow.value = _headingFlow.value + 0.15f * (azimuth - _headingFlow.value)
            _pitchFlow.value = _pitchFlow.value + 0.15f * (pitch - _pitchFlow.value)
            _rollFlow.value = _rollFlow.value + 0.15f * (roll - _rollFlow.value)

            // Rate-limit console logs to roughly every 250ms to prevent terminal flooding
            val now = System.currentTimeMillis()
            if (now - lastLogTime > 250) {
                lastLogTime = now
                Log.d(TAG, String.format("Heading: %03.1f° | Pitch: %02.1f° | Roll: %02.1f°", _headingFlow.value, _pitchFlow.value, _rollFlow.value))
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            val pitch = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
            val roll = Math.toDegrees(atan2(-ax.toDouble(), az.toDouble())).toFloat()

            _pitchFlow.value = pitch
            _rollFlow.value = roll
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val accuracyLabel = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE"
            else -> "UNKNOWN ($accuracy)"
        }
        Log.w(TAG, "Sensor Accuracy Changed: $accuracyLabel for sensor: ${sensor.name}")
        _isReliable.value = (accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE)
    }
}
