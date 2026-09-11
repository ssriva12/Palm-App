package com.palmlens.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.palmlens.BuildConfig

/**
 * Anchored adaptive banner for the home + text screens. Renders nothing until consent + SDK
 * init allow it (and nothing at all for premium users, once that exists).
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (!rememberAdManager().adsAllowed) return

    val widthDp = LocalConfiguration.current.screenWidthDp
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp),
                )
                adUnitId = BuildConfig.ADMOB_BANNER_UNIT
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = AdView::destroy,
    )
}
