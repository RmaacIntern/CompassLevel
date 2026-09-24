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
import kotlin.math.asin
import kotlin.math.atan2

data class CompassState(
    val heading: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val elevation: Float = 0f,
    val cameraRoll: Float = 0f,
    val isLevel: Boolean = false,
    val isReliable: Boolean = true,
    val accuracyLevel: String = "Optimal",
    val accuracyCode: Int = android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isTrueNorth: Boolean = false,
    val declination: Float = 0f,
    val usePercentGrade: Boolean = false,
    val isBearingLocked: Boolean = false,
    val lockedHeading: Float = 0f,
    val isAngleLocked: Boolean = false,
    val lockedPitch: Float = 0f,
    val lockedRoll: Float = 0f,
    val lockedElevation: Float = 0f,
    val lockedCameraRoll: Float = 0f
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Support both 9-axis gyroscope fusion and geomagnetic hardware fusion for devices without a gyro
    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasMagnetometer: Boolean = (rotationVectorSensor != null) || (magnetometer != null)
    val hasRotationVector: Boolean = rotationVectorSensor != null
    val hasAccelerometer: Boolean = accelerometer != null

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    // Backwards-compatible individual flows
    val headingFlow: StateFlow<Float> get() = _headingFlow
    private val _headingFlow = MutableStateFlow(0f)

    val pitchFlow: StateFlow<Float> get() = _pitchFlow
    private val _pitchFlow = MutableStateFlow(0f)

    val rollFlow: StateFlow<Float> get() = _rollFlow
    private val _rollFlow = MutableStateFlow(0f)

    val elevationFlow: StateFlow<Float> get() = _elevationFlow
    private val _elevationFlow = MutableStateFlow(0f)

    val cameraRollFlow: StateFlow<Float> get() = _cameraRollFlow
    private val _cameraRollFlow = MutableStateFlow(0f)

    val isReliable: StateFlow<Boolean> get() = _isReliable
    private val _isReliable = MutableStateFlow(true)

    private val rotationMatrix = FloatArray(9)

    // Filter buffers for raw fallback mode (accelerometer + magnetometer)
    private val filteredGravity = FloatArray(3)
    private val filteredGeomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Low-pass filter coefficients: isolates static gravity and suppresses dynamic hand movement spikes
    private val ALPHA_GRAVITY = 0.85f
    private val ALPHA_MAGNETIC = 0.82f

    // First sample initialization flags
    private var isHeadingInitialized = false
    private var isPitchInitialized = false
    private var isRollInitialized = false

    // Tare offsets for zero surface compensation
    private var tarePitch = 0f
    private var tareRoll = 0f
    private var rawPitch = 0f
    private var rawRoll = 0f

    // Settings
    private var isTrueNorth = false
    private var declination = 0f
    private var usePercentGrade = false

    // Bearing and Angle Locks
    private var isBearingLocked = false
    private var lockedHeading = 0f
    private var isAngleLocked = false
    private var lockedPitch = 0f
    private var lockedRoll = 0f
    private var lockedElevation = 0f
    private var lockedCameraRoll = 0f

    // Last emitted values for recomposition throttling
    private var lastEmittedHeading = 0f
    private var lastEmittedPitch = 0f
    private var lastEmittedRoll = 0f
    private var lastEmittedElevation = 0f
    private var lastEmittedCameraRoll = 0f
    private var lastEmittedIsLevel = false

    // Continuous adaptive angle filter without stick-slip deadband
    private fun filterAngle(current: Float, target: Float): Float {
        val delta = target - current
        val absDelta = abs(delta)
        if (absDelta < 0.02f) return current
        val alpha = when {
            absDelta >= 10f -> 0.75f
            absDelta <= 1.0f -> 0.16f + (absDelta / 1.0f) * 0.16f
            else -> 0.32f + ((absDelta - 1.0f) / 9.0f) * 0.43f
        }
        return current + alpha * delta
    }

    // Continuous adaptive circular filter for 0..360 Heading: eliminates stickiness when centering on North
    private fun filterHeading(current: Float, target: Float): Float {
        val delta = ((target - current + 540f) % 360f) - 180f
        val absDelta = abs(delta)
        if (absDelta < 0.02f) return current
        val alpha = when {
            absDelta >= 15f -> 0.75f
            absDelta <= 1.5f -> 0.16f + (absDelta / 1.5f) * 0.16f
            else -> 0.32f + ((absDelta - 1.5f) / 13.5f) * 0.43f
        }
        var smoothed = current + alpha * delta
        if (smoothed < 0f) smoothed += 360f
        if (smoothed >= 360f) smoothed -= 360f
        return smoothed
    }

    fun startListening() {
        hasGravity = false
        hasGeomagnetic = false
        isHeadingInitialized = false
        isPitchInitialized = false
        isRollInitialized = false

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
        isHeadingInitialized = false
        isPitchInitialized = false
        isRollInitialized = false
    }

    fun tare() {
        tarePitch = rawPitch
        tareRoll = rawRoll
    }

    fun resetTare() {
        tarePitch = 0f
        tareRoll = 0f
    }

    fun toggleBearingLock() {
        isBearingLocked = !isBearingLocked
        if (isBearingLocked) {
            lockedHeading = _headingFlow.value
        }
        _compassState.value = _compassState.value.copy(
            isBearingLocked = isBearingLocked,
            lockedHeading = lockedHeading
        )
    }

    fun toggleAngleLock() {
        isAngleLocked = !isAngleLocked
        if (isAngleLocked) {
            lockedPitch = _pitchFlow.value
            lockedRoll = _rollFlow.value
            lockedElevation = _elevationFlow.value
            lockedCameraRoll = _cameraRollFlow.value
        }
        _compassState.value = _compassState.value.copy(
            isAngleLocked = isAngleLocked,
            lockedPitch = lockedPitch,
            lockedRoll = lockedRoll,
            lockedElevation = lockedElevation,
            lockedCameraRoll = lockedCameraRoll
        )
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

    fun setGpsBearing(gpsBearing: Float) {
        if (!hasMagnetometer) {
            val norm = ((gpsBearing % 360f) + 360f) % 360f
            _headingFlow.value = norm
            _compassState.value = _compassState.value.copy(
                heading = norm,
                isReliable = true,
                accuracyLevel = "GPS Bearing"
            )
        }
    }

    fun setManualHeading(heading: Float) {
        if (!hasMagnetometer) {
            val norm = ((heading % 360f) + 360f) % 360f
            _headingFlow.value = norm
            _compassState.value = _compassState.value.copy(
                heading = norm,
                isReliable = true,
                accuracyLevel = "Manual Alignment"
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                processRotationMatrix(rotationMatrix)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (!hasGravity) {
                    System.arraycopy(event.values, 0, filteredGravity, 0, 3)
                    hasGravity = true
                } else {
                    filteredGravity[0] = ALPHA_GRAVITY * filteredGravity[0] + (1f - ALPHA_GRAVITY) * event.values[0]
                    filteredGravity[1] = ALPHA_GRAVITY * filteredGravity[1] + (1f - ALPHA_GRAVITY) * event.values[1]
                    filteredGravity[2] = ALPHA_GRAVITY * filteredGravity[2] + (1f - ALPHA_GRAVITY) * event.values[2]
                }
                if (hasGeomagnetic) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, filteredGravity, filteredGeomagnetic)) {
                        processRotationMatrix(rotationMatrix)
                    }
                } else {
                    processAccelerometerFallback(filteredGravity)
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (!hasGeomagnetic) {
                    System.arraycopy(event.values, 0, filteredGeomagnetic, 0, 3)
                    hasGeomagnetic = true
                } else {
                    filteredGeomagnetic[0] = ALPHA_MAGNETIC * filteredGeomagnetic[0] + (1f - ALPHA_MAGNETIC) * event.values[0]
                    filteredGeomagnetic[1] = ALPHA_MAGNETIC * filteredGeomagnetic[1] + (1f - ALPHA_MAGNETIC) * event.values[1]
                    filteredGeomagnetic[2] = ALPHA_MAGNETIC * filteredGeomagnetic[2] + (1f - ALPHA_MAGNETIC) * event.values[2]
                }
                if (hasGravity) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, filteredGravity, filteredGeomagnetic)) {
                        processRotationMatrix(rotationMatrix)
                    }
                }
            }
        }
    }

    private fun processRotationMatrix(r: FloatArray) {
        // Device axes in world coordinates:
        // Column 0 (Right):  (r[0], r[3], r[6])
        // Column 1 (Top):    (r[1], r[4], r[7])
        // Column 2 (Screen): (r[2], r[5], r[8])

        // 1. Tilt-Compensated Compass Heading:
        // The top edge of the device (12 o'clock / lubber chevron) in world coordinates is Column 1: (r[1], r[4], r[7]).
        // Its horizontal ground projection is (r[1], r[4]), where r[1] is East and r[4] is North.
        // atan2(r[1], r[4]) yields the continuous, tilt-compensated azimuth from North (0°) clockwise to East (90°).
        // This is continuous across ALL tilt angles and eliminates discontinuous coordinate flips.
        val horizontalNormSq = r[1] * r[1] + r[4] * r[4]
        var targetHeading = if (horizontalNormSq > 0.005f) {
            var h = Math.toDegrees(atan2(r[1].toDouble(), r[4].toDouble())).toFloat()
            if (h < 0f) h += 360f
            h
        } else {
            // Near zenith (straight up at sky), hold last stable heading
            if (isHeadingInitialized) _headingFlow.value else 0f
        }

        if (isTrueNorth) {
            targetHeading = (targetHeading + declination + 360f) % 360f
        }

        // 2. Flat Surface Spirit Level (Pitch & Roll):
        // Pitch: tilt of top edge above horizontal. Positive when top is lifted.
        val targetPitch = Math.toDegrees(asin(r[7].coerceIn(-1f, 1f).toDouble())).toFloat()
        // Roll: tilt of right edge above horizontal. Positive when right is lifted.
        val targetRoll = Math.toDegrees(asin(r[6].coerceIn(-1f, 1f).toDouble())).toFloat()

        rawPitch = targetPitch
        rawRoll = targetRoll

        val compensatedPitch = targetPitch - tarePitch
        val compensatedRoll = targetRoll - tareRoll

        // 3. Clinometer Sighting (Elevation & Camera Roll):
        // Elevation: angle of camera line of sight above horizontal (0° when looking level at horizon)
        val horizSightNorm = Math.hypot(r[2].toDouble(), r[5].toDouble())
        val targetElevation = Math.toDegrees(atan2(-r[8].toDouble(), horizSightNorm)).toFloat()

        // Camera Roll: tilt in portrait plane (0° in vertical portrait)
        val targetCameraRoll = Math.toDegrees(atan2(-r[6].toDouble(), r[7].toDouble())).toFloat()

        // Immediate crisp initialization on first sample
        if (!isHeadingInitialized) {
            _headingFlow.value = targetHeading
            lastEmittedHeading = targetHeading
            isHeadingInitialized = true
        }
        if (!isPitchInitialized) {
            _pitchFlow.value = compensatedPitch
            lastEmittedPitch = compensatedPitch
            isPitchInitialized = true
        }
        if (!isRollInitialized) {
            _rollFlow.value = compensatedRoll
            lastEmittedRoll = compensatedRoll
            isRollInitialized = true
        }

        // Smooth all outputs with continuous adaptive filter
        val smoothedHeading = filterHeading(_headingFlow.value, targetHeading)
        val smoothedPitch = filterAngle(_pitchFlow.value, compensatedPitch)
        val smoothedRoll = filterAngle(_rollFlow.value, compensatedRoll)
        val smoothedElevation = filterAngle(_elevationFlow.value, targetElevation)
        val smoothedCameraRoll = filterAngle(_cameraRollFlow.value, targetCameraRoll)

        _headingFlow.value = smoothedHeading
        _pitchFlow.value = smoothedPitch
        _rollFlow.value = smoothedRoll
        _elevationFlow.value = smoothedElevation
        _cameraRollFlow.value = smoothedCameraRoll

        val displayHeading = if (isBearingLocked) lockedHeading else smoothedHeading
        val displayPitch = if (isAngleLocked) lockedPitch else smoothedPitch
        val displayRoll = if (isAngleLocked) lockedRoll else smoothedRoll
        val displayElevation = if (isAngleLocked) lockedElevation else smoothedElevation
        val displayCameraRoll = if (isAngleLocked) lockedCameraRoll else smoothedCameraRoll

        val isLevel = abs(displayPitch) <= 0.6f && abs(displayRoll) <= 0.6f

        // Recomposition throttling to eliminate unnecessary Compose redraws
        val headingDelta = abs(((smoothedHeading - lastEmittedHeading + 540f) % 360f) - 180f)
        val pitchDelta = abs(smoothedPitch - lastEmittedPitch)
        val rollDelta = abs(smoothedRoll - lastEmittedRoll)
        val elevationDelta = abs(smoothedElevation - lastEmittedElevation)
        val cameraRollDelta = abs(smoothedCameraRoll - lastEmittedCameraRoll)

        if (headingDelta >= 0.05f || pitchDelta >= 0.04f || rollDelta >= 0.04f ||
            elevationDelta >= 0.04f || cameraRollDelta >= 0.04f || isLevel != lastEmittedIsLevel ||
            isBearingLocked != _compassState.value.isBearingLocked || isAngleLocked != _compassState.value.isAngleLocked
        ) {
            lastEmittedHeading = smoothedHeading
            lastEmittedPitch = smoothedPitch
            lastEmittedRoll = smoothedRoll
            lastEmittedElevation = smoothedElevation
            lastEmittedCameraRoll = smoothedCameraRoll
            lastEmittedIsLevel = isLevel

            _compassState.value = _compassState.value.copy(
                heading = displayHeading,
                pitch = displayPitch,
                roll = displayRoll,
                elevation = displayElevation,
                cameraRoll = displayCameraRoll,
                isLevel = isLevel,
                isReliable = _isReliable.value,
                isTrueNorth = isTrueNorth,
                declination = declination,
                usePercentGrade = usePercentGrade,
                isBearingLocked = isBearingLocked,
                lockedHeading = lockedHeading,
                isAngleLocked = isAngleLocked,
                lockedPitch = lockedPitch,
                lockedRoll = lockedRoll,
                lockedElevation = lockedElevation,
                lockedCameraRoll = lockedCameraRoll
            )
        }
    }

    private fun processAccelerometerFallback(accel: FloatArray) {
        val ax = accel[0] / 9.80665f
        val ay = accel[1] / 9.80665f
        val az = accel[2] / 9.80665f

        val targetPitch = Math.toDegrees(asin(ay.coerceIn(-1f, 1f).toDouble())).toFloat()
        val targetRoll = Math.toDegrees(asin((-ax).coerceIn(-1f, 1f).toDouble())).toFloat()
        val targetElevation = Math.toDegrees(asin((-az).coerceIn(-1f, 1f).toDouble())).toFloat()
        val targetCameraRoll = Math.toDegrees(atan2(ax.toDouble(), ay.toDouble())).toFloat()

        rawPitch = targetPitch
        rawRoll = targetRoll

        val compensatedPitch = targetPitch - tarePitch
        val compensatedRoll = targetRoll - tareRoll

        if (!isPitchInitialized) {
            _pitchFlow.value = compensatedPitch
            lastEmittedPitch = compensatedPitch
            isPitchInitialized = true
        }
        if (!isRollInitialized) {
            _rollFlow.value = compensatedRoll
            lastEmittedRoll = compensatedRoll
            isRollInitialized = true
        }

        val smoothedPitch = filterAngle(_pitchFlow.value, compensatedPitch)
        val smoothedRoll = filterAngle(_rollFlow.value, compensatedRoll)
        val smoothedElevation = filterAngle(_elevationFlow.value, targetElevation)
        val smoothedCameraRoll = filterAngle(_cameraRollFlow.value, targetCameraRoll)

        _pitchFlow.value = smoothedPitch
        _rollFlow.value = smoothedRoll
        _elevationFlow.value = smoothedElevation
        _cameraRollFlow.value = smoothedCameraRoll

        val displayPitch = if (isAngleLocked) lockedPitch else smoothedPitch
        val displayRoll = if (isAngleLocked) lockedRoll else smoothedRoll
        val displayElevation = if (isAngleLocked) lockedElevation else smoothedElevation
        val displayCameraRoll = if (isAngleLocked) lockedCameraRoll else smoothedCameraRoll

        val isLevel = abs(displayPitch) <= 0.6f && abs(displayRoll) <= 0.6f

        val pitchDelta = abs(smoothedPitch - lastEmittedPitch)
        val rollDelta = abs(smoothedRoll - lastEmittedRoll)
        val elevationDelta = abs(smoothedElevation - lastEmittedElevation)
        val cameraRollDelta = abs(smoothedCameraRoll - lastEmittedCameraRoll)

        if (pitchDelta >= 0.04f || rollDelta >= 0.04f || elevationDelta >= 0.04f ||
            cameraRollDelta >= 0.04f || isLevel != lastEmittedIsLevel ||
            isBearingLocked != _compassState.value.isBearingLocked || isAngleLocked != _compassState.value.isAngleLocked
        ) {
            lastEmittedPitch = smoothedPitch
            lastEmittedRoll = smoothedRoll
            lastEmittedElevation = smoothedElevation
            lastEmittedCameraRoll = smoothedCameraRoll
            lastEmittedIsLevel = isLevel

            _compassState.value = _compassState.value.copy(
                pitch = displayPitch,
                roll = displayRoll,
                elevation = displayElevation,
                cameraRoll = displayCameraRoll,
                isLevel = isLevel
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val (stateStr, reliable) = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Optimal" to true
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Good" to true
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Active" to true
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Tuning" to true // Calm, never panic user
            else -> "Active" to true
        }
        _isReliable.value = reliable
        _compassState.value = _compassState.value.copy(
            isReliable = reliable,
            accuracyLevel = stateStr,
            accuracyCode = accuracy
        )
    }
}
