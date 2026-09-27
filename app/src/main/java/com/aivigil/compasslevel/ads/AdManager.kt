package com.aivigil.compasslevel.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdManager: Handles Google AdMob SDK integration only.
 * - Initializes AdMob SDK
 * - Preloads and shows real AdMob Interstitial Ads
 * - No simulated/fake ad creatives — only real AdMob ads
 */
class AdManager(private val context: Context) {

    // Real AdMob Interstitial — Google test unit ID (replace with your real ID before publishing)
    private val interstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialTimeMs: Long = 0L
    private var interactionCount: Int = 0
    private val cooldownMs = 30_000L

    init {
        try {
            MobileAds.initialize(context) {
                loadInterstitial()
            }
        } catch (e: Exception) {
            // AdMob SDK init failed silently — no crash
        }
    }

    private fun loadInterstitial() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Shows a real AdMob Interstitial immediately after the splash screen completes.
     * If the ad hasn't loaded yet (e.g. slow network), just calls onProceed directly.
     */
    fun showPostSplashInterstitial(activity: Activity?, onProceed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null && activity != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial()
                    lastInterstitialTimeMs = System.currentTimeMillis()
                    onProceed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial()
                    onProceed()
                }
            }
            ad.show(activity)
        } else {
            // Ad not ready yet — proceed without blocking the user
            onProceed()
        }
    }

    /**
     * Shows an AdMob Interstitial on tool transitions, respecting the 30s cooldown.
     * If the cooldown is active or ad isn't loaded, proceeds immediately.
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

    fun release() {
        interstitialAd = null
    }
}
