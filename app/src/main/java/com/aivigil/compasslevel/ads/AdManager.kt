package com.aivigil.compasslevel.ads

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

data class AdCreative(
    val id: String,
    val title: String,
    val subtitle: String,
    val sponsorTag: String,
    val category: String,
    val rating: Float = 4.9f,
    val downloads: String = "1M+ Downloads",
    val ctaText: String = "INSTALL NOW",
    val accentColorHex: Long = 0xFF00E5FF
)

/**
 * AdManager: Offline simulation of commercial ad monetization (Google AdMob / Unity Ads model).
 * Manages banner campaign rotation, interstitial frequency capping (35s cooldown), and dialog callbacks.
 */
class AdManager(private val context: Context) {

    val sampleCreatives = listOf(
        AdCreative(
            id = "ad_gps_pro",
            title = "Ultra Precision GPS Pro",
            subtitle = "Military-grade waypoint tracking, topo contours & 100% offline trail navigation.",
            sponsorTag = "Apex GeoSystems",
            category = "Navigation & Maps",
            rating = 4.9f,
            downloads = "2.4M+ Downloads",
            ctaText = "INSTALL NOW",
            accentColorHex = 0xFF00E5FF
        ),
        AdCreative(
            id = "ad_level_3d",
            title = "Laser Level 3D Toolkit",
            subtitle = "Calibrated digital laser levels, pitch angle measurement & AR surface alignment.",
            sponsorTag = "SurveyTech Global",
            category = "Engineering & Tools",
            rating = 4.8f,
            downloads = "850K+ Downloads",
            ctaText = "TRY FREE",
            accentColorHex = 0xFF00E676
        ),
        AdCreative(
            id = "ad_topomaps",
            title = "TopoMaps Offline Pro",
            subtitle = "Download high-res satellite 3D topography and GPS waypoints for wilderness hiking.",
            sponsorTag = "Orion Instruments",
            category = "Travel & Local",
            rating = 4.9f,
            downloads = "1.2M+ Downloads",
            ctaText = "DOWNLOAD",
            accentColorHex = 0xFFFFD700
        ),
        AdCreative(
            id = "ad_field_toolkit",
            title = "Field Engineer Utility Suite",
            subtitle = "Comprehensive unit conversions, structural clinometers and acoustic alignment.",
            sponsorTag = "FieldVector Labs",
            category = "Productivity & Utilities",
            rating = 4.9f,
            downloads = "500K+ Downloads",
            ctaText = "INSTALL NOW",
            accentColorHex = 0xFFFF7043
        )
    )

    // Current creative for Banner rotation
    var currentBannerCreative by mutableStateOf(sampleCreatives[0])
        private set

    // Current creative for Interstitial display
    var currentInterstitialCreative by mutableStateOf(sampleCreatives[1])
        private set

    var isInterstitialVisible by mutableStateOf(false)
        private set

    private var onInterstitialDismissed: (() -> Unit)? = null
    private var lastInterstitialTimeMs: Long = 0L
    private var interactionCount: Int by mutableIntStateOf(0)

    // Cooldown duration between interstitials (35 seconds as selected by user)
    private val cooldownMs = 35_000L
    // Require at least 2 interactions or cooldown elapsed
    private val minInteractionsBeforeAd = 2

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var bannerRotationJob: Job? = null

    init {
        startBannerRotation()
    }

    private fun startBannerRotation() {
        bannerRotationJob?.cancel()
        bannerRotationJob = scope.launch {
            var index = 0
            while (isActive) {
                delay(20_000L) // Rotate banner every 20 seconds
                index = (index + 1) % sampleCreatives.size
                currentBannerCreative = sampleCreatives[index]
            }
        }
    }

    /**
     * Checks if cooldown and interaction criteria are met.
     * If eligible, displays the full-screen interstitial ad and executes [onProceed] when dismissed.
     * If within cooldown, immediately executes [onProceed] with zero delay to maintain silky-smooth UX.
     */
    fun maybeShowInterstitial(onProceed: () -> Unit) {
        val now = System.currentTimeMillis()
        interactionCount++

        val cooldownElapsed = (now - lastInterstitialTimeMs) >= cooldownMs
        val enoughInteractions = interactionCount >= minInteractionsBeforeAd

        if (cooldownElapsed && enoughInteractions) {
            // Pick next creative for variety
            val nextIndex = (sampleCreatives.indexOf(currentInterstitialCreative) + 1) % sampleCreatives.size
            currentInterstitialCreative = sampleCreatives[nextIndex]
            onInterstitialDismissed = onProceed
            lastInterstitialTimeMs = now
            interactionCount = 0
            isInterstitialVisible = true
        } else {
            // Cooldown active: proceed immediately without disrupting the user
            onProceed()
        }
    }

    /**
     * Force-displays an interstitial ad (e.g. for specific milestone buttons)
     */
    fun forceShowInterstitial(onProceed: () -> Unit) {
        val now = System.currentTimeMillis()
        lastInterstitialTimeMs = now
        interactionCount = 0
        onInterstitialDismissed = onProceed
        isInterstitialVisible = true
    }

    /**
     * Called when the user clicks Skip, Close, or the countdown completes and they dismiss the ad.
     */
    fun dismissInterstitial() {
        if (isInterstitialVisible) {
            isInterstitialVisible = false
            val callback = onInterstitialDismissed
            onInterstitialDismissed = null
            callback?.invoke()
        }
    }

    fun release() {
        bannerRotationJob?.cancel()
    }
}
