package com.aivigil.compasslevel.sensor

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.aivigil.compasslevel.R
import kotlin.math.abs

/**
 * Safe, high-performance mechanical rotary dial sound and haptic feedback manager.
 * Produces crisp, satisfying ratchet clicks as the compass rotates across precision sectors,
 * with safety rate-limiting and angular hysteresis to prevent audio buffer saturation or jitter clicks.
 */
class CompassSoundManager(private val context: Context) {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttributes)
        .build()

    private var clickSoundId: Int = 0
    private var northSoundId: Int = 0
    private var isLoaded: Boolean = false

    private var isSoundEnabled: Boolean = true
    private var isHapticEnabled: Boolean = true

    // Safety and acoustic calibration constants
    private val DEGREES_PER_TICK = 5.0f    // Distinct click every 5° interval
    private val MIN_INTERVAL_MS = 40L      // Max ~25 clicks/sec to prevent audio buzzing on rapid spins
    private val HYSTERESIS_DEG = 1.8f      // Requires at least 1.8° movement to prevent boundary jitter

    private var lastClickTimeMs: Long = 0L
    private var lastSectorIndex: Int = -1
    private var lastTriggerHeading: Float = -999f

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    init {
        loadSounds()
    }

    private fun loadSounds() {
        try {
            clickSoundId = soundPool.load(context, R.raw.compass_click, 1)
            northSoundId = soundPool.load(context, R.raw.compass_north, 1)
            soundPool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                }
            }
        } catch (e: Exception) {
            // Gracefully ignore sound loading failure
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun isSoundEnabled(): Boolean = isSoundEnabled

    fun setHapticEnabled(enabled: Boolean) {
        isHapticEnabled = enabled
    }

    fun isHapticEnabled(): Boolean = isHapticEnabled

    /**
     * Evaluates heading rotation and safely plays a rotary ratchet click.
     * Returns true if a click occurred.
     */
    fun onHeadingChanged(heading: Float): Boolean {
        val normalized = ((heading % 360f) + 360f) % 360f
        val currentSector = (normalized / DEGREES_PER_TICK).toInt()

        // First sample initialization
        if (lastSectorIndex == -1 || lastTriggerHeading < 0f) {
            lastSectorIndex = currentSector
            lastTriggerHeading = normalized
            return false
        }

        // Check if sector changed
        if (currentSector != lastSectorIndex) {
            val now = System.currentTimeMillis()

            // Safety Constraint 1: Minimum time interval (debounce against rapid spinning)
            if (now - lastClickTimeMs < MIN_INTERVAL_MS) {
                return false
            }

            // Safety Constraint 2: Angular hysteresis (prevents micro-jitter clicks when held stationary)
            val angularDelta = abs(((normalized - lastTriggerHeading + 540f) % 360f) - 180f)
            if (angularDelta < HYSTERESIS_DEG) {
                return false
            }

            lastClickTimeMs = now
            lastSectorIndex = currentSector
            lastTriggerHeading = normalized

            // North Detection: within +/- 2.5 degrees of 0 / 360
            val isNorth = normalized <= 2.5f || normalized >= 357.5f

            if (isSoundEnabled && isLoaded) {
                if (isNorth) {
                    playNorthSound()
                } else {
                    playClickSound()
                }
            }

            if (isHapticEnabled) {
                triggerHaptic(isNorth)
            }

            return true
        }

        return false
    }

    private fun playClickSound() {
        try {
            if (clickSoundId != 0 && isLoaded) {
                soundPool.play(clickSoundId, 0.40f, 0.40f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun playNorthSound() {
        try {
            if (northSoundId != 0 && isLoaded) {
                soundPool.play(northSoundId, 0.55f, 0.55f, 2, 0, 1.0f)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun triggerHaptic(isNorth: Boolean) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = if (isNorth) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(if (isNorth) 18L else 8L)
            }
        } catch (e: Exception) {
            // Ignore haptic error safely
        }
    }

    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {
            // Safe cleanup
        }
    }
}
