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
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
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
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

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

    // Adaptive Angle Filter with Deadband
    private fun filterAngle(current: Float, target: Float, deadband: Float): Float {
        val delta = target - current
        val absDelta = abs(delta)
        if (absDelta < deadband) return current
        val alpha = when {
            absDelta >= 10f -> 0.76f
            absDelta <= 1.0f -> 0.18f + (absDelta / 1.0f) * 0.14f
            else -> 0.32f + ((absDelta - 1.0f) / 9.0f) * 0.44f
        }
        return current + alpha * delta
    }

    // Adaptive Circular Filter for 0..360 Heading with Deadband
    private fun filterHeading(current: Float, target: Float, deadband: Float): Float {
        val delta = ((target - current + 540f) % 360f) - 180f
        val absDelta = abs(delta)
        if (absDelta < deadband) return current
        val alpha = when {
            absDelta >= 15f -> 0.78f
            absDelta <= 1.5f -> 0.16f + (absDelta / 1.5f) * 0.16f
            else -> 0.32f + ((absDelta - 1.5f) / 13.5f) * 0.46f
        }
        var smoothed = current + alpha * delta
        if (smoothed < 0f) smoothed += 360f
        if (smoothed >= 360f) smoothed -= 360f
        return smoothed
    }

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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            processRotationMatrix(rotationMatrix)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
            hasGravity = true
            if (hasGeomagnetic) {
                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                    processRotationMatrix(rotationMatrix)
                }
            } else {
                processAccelerometerFallback(event.values)
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
            hasGeomagnetic = true
            if (hasGravity) {
                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                    processRotationMatrix(rotationMatrix)
                }
            }
        }
    }

    private fun processRotationMatrix(r: FloatArray) {
        // Device axes in world coordinates:
        // Column 0 (Right):  (r[0], r[3], r[6])
        // Column 1 (Top):    (r[1], r[4], r[7])
        // Column 2 (Screen): (r[2], r[5], r[8])
        // Line of sight through camera (-Z): (-r[2], -r[5], -r[8])

        // 1. Tilt-Compensated Compass Heading:
        // Seamlessly blends horizontal projection from flat (top forward) to upright (sight forward)
        val topE = r[1]
        val topN = r[4]
        val sightE = -r[2]
        val sightN = -r[5]

        val tiltWeight = (r[7] * r[7]).coerceIn(0f, 1f)
        val forwardE = (1f - tiltWeight) * topE + tiltWeight * sightE
        val forwardN = (1f - tiltWeight) * topN + tiltWeight * sightN

        var targetHeading = Math.toDegrees(atan2(forwardE.toDouble(), forwardN.toDouble())).toFloat()
        if (targetHeading < 0f) targetHeading += 360f

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

        // Smooth all outputs with adaptive low-jitter filter & deadband
        val smoothedHeading = filterHeading(_headingFlow.value, targetHeading, deadband = 0.12f)
        val smoothedPitch = filterAngle(_pitchFlow.value, compensatedPitch, deadband = 0.06f)
        val smoothedRoll = filterAngle(_rollFlow.value, compensatedRoll, deadband = 0.06f)
        val smoothedElevation = filterAngle(_elevationFlow.value, targetElevation, deadband = 0.06f)
        val smoothedCameraRoll = filterAngle(_cameraRollFlow.value, targetCameraRoll, deadband = 0.06f)

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

        // Check if values changed noticeably to prevent unnecessary Compose recompositions
        val headingDelta = abs(((smoothedHeading - lastEmittedHeading + 540f) % 360f) - 180f)
        val pitchDelta = abs(smoothedPitch - lastEmittedPitch)
        val rollDelta = abs(smoothedRoll - lastEmittedRoll)
        val elevationDelta = abs(smoothedElevation - lastEmittedElevation)
        val cameraRollDelta = abs(smoothedCameraRoll - lastEmittedCameraRoll)

        if (headingDelta >= 0.06f || pitchDelta >= 0.05f || rollDelta >= 0.05f ||
            elevationDelta >= 0.05f || cameraRollDelta >= 0.05f || isLevel != lastEmittedIsLevel ||
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

        val smoothedPitch = filterAngle(_pitchFlow.value, compensatedPitch, deadband = 0.06f)
        val smoothedRoll = filterAngle(_rollFlow.value, compensatedRoll, deadband = 0.06f)
        val smoothedElevation = filterAngle(_elevationFlow.value, targetElevation, deadband = 0.06f)
        val smoothedCameraRoll = filterAngle(_cameraRollFlow.value, targetCameraRoll, deadband = 0.06f)

        _pitchFlow.value = smoothedPitch
        _rollFlow.value = smoothedRoll
        _elevationFlow.value = smoothedElevation
        _cameraRollFlow.value = smoothedCameraRoll

        val displayPitch = if (isAngleLocked) lockedPitch else smoothedPitch
        val displayRoll = if (isAngleLocked) lockedRoll else smoothedRoll
        val displayElevation = if (isAngleLocked) lockedElevation else smoothedElevation
        val displayCameraRoll = if (isAngleLocked) lockedCameraRoll else smoothedCameraRoll

        val isLevel = abs(displayPitch) <= 0.6f && abs(displayRoll) <= 0.6f

        _compassState.value = _compassState.value.copy(
            pitch = displayPitch,
            roll = displayRoll,
            elevation = displayElevation,
            cameraRoll = displayCameraRoll,
            isLevel = isLevel
        )
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
