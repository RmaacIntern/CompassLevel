package com.aivigil.compasslevel.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
 * AdManager: Handles Google Mobile Ads (AdMob) SDK integration and smart offline fallback.
 * - Initializes AdMob SDK
 * - Preloads real AdMob Interstitial Ads (Sample Unit ID: ca-app-pub-3940256099942544/1033173712)
 * - Displays Interstitial right after Splash screen (as requested by leadership)
 * - Provides seamless fallback to interactive in-app dialog if offline or rate-limited
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

    // Real AdMob Interstitial instance
    private var admobInterstitial: InterstitialAd? = null
    private val interstitialSampleUnitId = "ca-app-pub-3940256099942544/1033173712"

    private val cooldownMs = 30_000L
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var bannerRotationJob: Job? = null

    init {
        // Initialize Google Mobile Ads SDK
        try {
            MobileAds.initialize(context) {
                loadAdmobInterstitial()
            }
        } catch (e: Exception) {
            // Silently fall back to simulation
        }
        startBannerRotation()
    }

    private fun loadAdmobInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialSampleUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    admobInterstitial = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    admobInterstitial = null
                }
            }
        )
    }

    private fun startBannerRotation() {
        bannerRotationJob?.cancel()
        bannerRotationJob = scope.launch {
            var index = 0
            while (isActive) {
                delay(20_000L)
                index = (index + 1) % sampleCreatives.size
                currentBannerCreative = sampleCreatives[index]
            }
        }
    }

    /**
     * Shows an Interstitial Ad immediately after the Splash screen completes.
     * Tries live AdMob interstitial first; if not loaded/offline, displays our in-app interstitial dialog.
     */
    fun showPostSplashInterstitial(activity: Activity?, onProceed: () -> Unit) {
        val ad = admobInterstitial
        if (ad != null && activity != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    admobInterstitial = null
                    loadAdmobInterstitial()
                    lastInterstitialTimeMs = System.currentTimeMillis()
                    onProceed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    admobInterstitial = null
                    loadAdmobInterstitial()
                    onProceed()
                }
            }
            ad.show(activity)
        } else {
            // Fallback to rich in-app interstitial
            onInterstitialDismissed = onProceed
            lastInterstitialTimeMs = System.currentTimeMillis()
            isInterstitialVisible = true
        }
    }

    /**
     * Checks cooldown and interaction count before showing an interstitial on tool transitions.
     */
    fun maybeShowInterstitial(activity: Activity?, onProceed: () -> Unit) {
        val now = System.currentTimeMillis()
        interactionCount++

        val cooldownElapsed = (now - lastInterstitialTimeMs) >= cooldownMs
        val enoughInteractions = interactionCount >= 2

        if (cooldownElapsed && enoughInteractions) {
            interactionCount = 0
            showPostSplashInterstitial(activity, onProceed)
        } else {
            onProceed()
        }
    }

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
