package com.palmlens.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.palmlens.BuildConfig
import com.palmlens.core.coroutines.DefaultDispatcher
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

// All ads (banner + interstitial) cut out of the current flow, still test units. This is the
// single switch for every ad surface — flip back to true to re-enable everywhere at once.
private const val ADS_ENABLED = false

/**
 * The single chokepoint for every ad in the app: UMP consent, one-time SDK init, the
 * interstitial shown on scan-result exit, and the "privacy options" hook for Settings.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) {
    // ponytail: hardcoded until Play Billing (Phase 6) lands. Every ad path routes through
    //   here, so swap this for an injected EntitlementRepository.isPremium — nothing else moves.
    private val isPremium: Boolean get() = false

    /** True once the SDK is initialised and consent permits ad requests. Compose-observable. */
    var ready by mutableStateOf(false)
        private set

    val adsAllowed: Boolean get() = ADS_ENABLED && ready && !isPremium

    /** One-line interstitial state for the debug overlay (spec §7). */
    val interstitialStatus: String
        get() = when {
            !adsAllowed -> "ads off"
            interstitial != null -> "loaded"
            interstitialLoading -> "loading"
            else -> "none"
        }

    private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)
    private val initStarted = AtomicBoolean(false)
    private var consentInformation: ConsentInformation? = null

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false

    /**
     * Call from an Activity on launch: refresh consent, show the form when the region needs
     * it, then initialise the Ads SDK. Idempotent — safe to call every launch.
     */
    fun gatherConsentAndInitialize(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity).also { consentInformation = it }
        info.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Log.w("Palmlens", "consent form: ${it.message}") }
                    if (info.canRequestAds()) initialize()
                }
            },
            { error -> Log.w("Palmlens", "consent info update failed: ${error.message}") },
        )
        if (info.canRequestAds()) initialize() // fast path once consent is already resolved
    }

    /** Whether Settings should show a "Privacy options" entry (wired in Phase 8). */
    val privacyOptionsRequired: Boolean
        get() = consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            error?.let { Log.w("Palmlens", "privacy options form: ${it.message}") }
        }
    }

    private fun initialize() {
        if (!initStarted.compareAndSet(false, true)) return
        // MobileAds.initialize touches disk; keep it off the main thread. Its callback lands on main.
        scope.launch {
            MobileAds.initialize(context) {
                ready = true
                preloadInterstitial()
            }
        }
    }

    /** Warm an interstitial (call when a scan starts, so it's ready by the time the reading is). */
    fun preloadInterstitial() {
        if (!adsAllowed || interstitial != null || interstitialLoading) return
        interstitialLoading = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("Palmlens", "interstitial load failed: ${error.message}")
                    interstitial = null
                    interstitialLoading = false
                }
            },
        )
    }

    /** Show the preloaded interstitial then run [onDone]; if nothing is ready, run [onDone] now. */
    fun showInterstitial(activity: Activity, onDone: () -> Unit) {
        val ad = interstitial
        if (!adsAllowed || ad == null) {
            onDone()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preloadInterstitial()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w("Palmlens", "interstitial show failed: ${error.message}")
                interstitial = null
                onDone()
            }
        }
        ad.show(activity)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Provider {
        fun adManager(): AdManager
    }
}
