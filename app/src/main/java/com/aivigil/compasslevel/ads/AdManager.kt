package com.aivigil.compasslevel.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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
 * - Guaranteed post-splash interstitial (waits briefly if ad is still loading)
 * - 20s cooldown between interstitials for tool/settings/theme clicks
 */
class AdManager(private val context: Context) {

    // Real AdMob Interstitial — Google test unit ID (replace with your real ID before publishing)
    private val interstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false
    private var lastInterstitialTimeMs: Long = 0L
    private val cooldownMs = 20_000L   // 20 seconds between ads

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingPostSplashActivity: Activity? = null
    private var pendingPostSplashCallback: (() -> Unit)? = null
    private var splashTimeoutRunnable: Runnable? = null

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
        if (isAdLoading || interstitialAd != null) return
        isAdLoading = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false

                    // If splash completed while ad was loading, display it immediately
                    val pendingActivity = pendingPostSplashActivity
                    val pendingCallback = pendingPostSplashCallback
                    if (pendingActivity != null && pendingCallback != null) {
                        clearPendingSplash()
                        showAd(pendingActivity, pendingCallback)
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false

                    // Proceed if splash was waiting on this ad
                    val pendingCallback = pendingPostSplashCallback
                    if (pendingCallback != null) {
                        clearPendingSplash()
                        pendingCallback.invoke()
                    }
                }
            }
        )
    }

    private fun clearPendingSplash() {
        splashTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        splashTimeoutRunnable = null
        pendingPostSplashActivity = null
        pendingPostSplashCallback = null
    }

    /**
     * Internal helper: actually shows the interstitial and calls onProceed when done.
     */
    private fun showAd(activity: Activity?, onProceed: () -> Unit) {
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
            onProceed()
        }
    }

    /**
     * Shows an AdMob Interstitial immediately after the splash screen completes.
     * If the ad is still loading, waits up to 2.5s to show it, matching the reference video.
     */
    fun showPostSplashInterstitial(activity: Activity?, onProceed: () -> Unit) {
        if (interstitialAd != null && activity != null) {
            showAd(activity, onProceed)
        } else if (isAdLoading && activity != null) {
            // Ad is currently loading over network — wait up to 2.5s for it to finish
            pendingPostSplashActivity = activity
            pendingPostSplashCallback = onProceed

            val timeout = Runnable {
                val cb = pendingPostSplashCallback
                clearPendingSplash()
                cb?.invoke()
            }
            splashTimeoutRunnable = timeout
            mainHandler.postDelayed(timeout, 2500L)
        } else {
            onProceed()
        }
    }

    /**
     * Shows an AdMob Interstitial on ANY user interaction (tool switch, settings, themes, etc.).
     * Respects a 20-second cooldown so users aren't spammed on rapid taps.
     */
    fun maybeShowInterstitial(activity: Activity?, onProceed: () -> Unit) {
        val now = System.currentTimeMillis()
        val cooldownElapsed = (now - lastInterstitialTimeMs) >= cooldownMs

        if (cooldownElapsed && interstitialAd != null) {
            showAd(activity, onProceed)
        } else {
            if (interstitialAd == null && !isAdLoading) {
                loadInterstitial()
            }
            onProceed()
        }
    }

    fun release() {
        clearPendingSplash()
        interstitialAd = null
    }
}
