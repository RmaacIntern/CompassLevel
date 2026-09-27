package com.aivigil.compasslevel.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aivigil.compasslevel.ui.theme.SkinPalette
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdmobAdaptiveBannerView: Google AdMob Anchored Adaptive Banner.
 * Uses Google's official test banner unit ID. Replace with your real AdMob banner unit ID before publishing.
 * Shows a minimal placeholder background while the ad is loading.
 */
@Composable
fun AdmobAdaptiveBannerView(
    skin: SkinPalette,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/9214589741" // Google test banner — replace with real ID
) {
    val context = LocalContext.current
    var adLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(skin.cardBackground),
        contentAlignment = Alignment.Center
    ) {
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
                            adLoaded = true
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            adLoaded = false
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            }
        )

        // Slim placeholder shown while AdMob is loading (avoids layout shift)
        if (!adLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(skin.cardBackground)
            )
        }
    }
}
