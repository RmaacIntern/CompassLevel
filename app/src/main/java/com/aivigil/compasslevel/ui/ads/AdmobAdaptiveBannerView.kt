package com.aivigil.compasslevel.ui.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aivigil.compasslevel.ads.AdCreative
import com.aivigil.compasslevel.ui.theme.SkinPalette
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdmobAdaptiveBannerView: Google AdMob Anchored Adaptive Banner integration.
 * Calculates orientation-adaptive width and requests a real AdMob banner using Google's sample ad unit ID.
 * If AdMob has no fill or device is offline, seamlessly displays our high-res simulated fallback creative.
 */
@Composable
fun AdmobAdaptiveBannerView(
    fallbackCreative: AdCreative,
    skin: SkinPalette,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/9214589741" // Official Google AdMob Adaptive Banner sample unit
) {
    val context = LocalContext.current
    var isAdMobLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(skin.cardBackground)
    ) {
        // Real Google AdMob Adaptive Banner
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    this.adUnitId = adUnitId
                    val displayMetrics = ctx.resources.displayMetrics
                    val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth)
                    setAdSize(adaptiveSize)

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdMobLoaded = true
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            isAdMobLoaded = false
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            }
        )

        // Seamless fallback if AdMob is loading or offline
        if (!isAdMobLoaded) {
            BannerAdView(
                creative = fallbackCreative,
                skin = skin
            )
        }
    }
}
