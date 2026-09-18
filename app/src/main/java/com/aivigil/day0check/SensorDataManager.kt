package com.aivigil.day0check

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class SensorSnapshot(
    val heading: Float,
    val pitch: Float,
    val roll: Float,
    val isLevel: Boolean,
    val accuracy: Int,
    val isCompassAvailable: Boolean,
    val isUnreliable: Boolean
)

class SensorDataManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasRequiredSensors: Boolean
        get() = rotationSensor != null || accelSensor != null

    val hasCompass: Boolean
        get() = rotationSensor != null || (accelSensor != null && magSensor != null)

    fun getSensorStream(): Flow<SensorSnapshot> = callbackFlow {
        var lastGoodHeading = 0f
        var currentSmoothedHeading = 0f
        var smoothedPitch = 0f
        var smoothedRoll = 0f

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val accelReading = FloatArray(3)
        val magReading = FloatArray(3)
        var hasAccel = false
        var hasMag = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val accuracy = event.accuracy
                val isUnreliable = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    processOrientation(orientationAngles, accuracy, isUnreliable, hasCompassHardware = true)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelReading, 0, 3)
                    hasAccel = true

                    if (magSensor == null) {
                        // Accelerometer-only trigonometry fallback for Level
                        val ax = accelReading[0].toDouble()
                        val ay = accelReading[1].toDouble()
                        val az = accelReading[2].toDouble()

                        val pitch = Math.toDegrees(atan2(-ay, sqrt(ax * ax + az * az))).toFloat()
                        val roll = Math.toDegrees(atan2(ax, az)).toFloat()

                        processLevelOnly(pitch, roll, accuracy)
                    } else if (hasMag) {
                        val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelReading, magReading)
                        if (success) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            processOrientation(orientationAngles, accuracy, isUnreliable, hasCompassHardware = true)
                        }
                    }
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magReading, 0, 3)
                    hasMag = true
                }
            }

            private fun processOrientation(
                angles: FloatArray,
                accuracy: Int,
                isUnreliable: Boolean,
                hasCompassHardware: Boolean
            ) {
                var rawAzimuth = Math.toDegrees(angles[0].toDouble()).toFloat()
                if (rawAzimuth < 0) rawAzimuth += 360f

                val rawPitch = Math.toDegrees(angles[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(angles[2].toDouble()).toFloat()

                // If sensor becomes UNRELIABLE, hold the last good heading to prevent erratic wild spinning
                val targetHeading = if (isUnreliable) lastGoodHeading else rawAzimuth

                // Low-pass filter (0.12f smoothing factor for stable readout)
                val alpha = 0.12f
                currentSmoothedHeading += alpha * shortestAngleDiff(targetHeading, currentSmoothedHeading)
                currentSmoothedHeading = (currentSmoothedHeading + 360f) % 360f

                if (!isUnreliable) {
                    lastGoodHeading = currentSmoothedHeading
                }

                smoothedPitch += alpha * (rawPitch - smoothedPitch)
                smoothedRoll += alpha * (rawRoll - smoothedRoll)

                val isLevel = abs(smoothedPitch) <= 0.5f && abs(smoothedRoll) <= 0.5f

                trySend(
                    SensorSnapshot(
                        heading = currentSmoothedHeading,
                        pitch = smoothedPitch,
                        roll = smoothedRoll,
                        isLevel = isLevel,
                        accuracy = accuracy,
                        isCompassAvailable = hasCompassHardware,
                        isUnreliable = isUnreliable
                    )
                )
            }

            private fun processLevelOnly(rawPitch: Float, rawRoll: Float, accuracy: Int) {
                val alpha = 0.15f
                smoothedPitch += alpha * (rawPitch - smoothedPitch)
                smoothedRoll += alpha * (rawRoll - smoothedRoll)

                val isLevel = abs(smoothedPitch) <= 0.5f && abs(smoothedRoll) <= 0.5f

                trySend(
                    SensorSnapshot(
                        heading = 0f,
                        pitch = smoothedPitch,
                        roll = smoothedRoll,
                        isLevel = isLevel,
                        accuracy = accuracy,
                        isCompassAvailable = false,
                        isUnreliable = false
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun shortestAngleDiff(target: Float, current: Float): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }
}
