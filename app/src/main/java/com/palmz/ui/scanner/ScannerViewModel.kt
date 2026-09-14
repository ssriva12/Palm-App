package com.palmz.ui.scanner

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.ads.AdManager
import com.palmz.core.coroutines.DefaultDispatcher
import com.palmz.data.image.ImagePreprocessor
import com.palmz.debug.DebugDiagnostics
import com.palmz.domain.model.Hand
import com.palmz.domain.model.PalmReading
import com.palmz.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmz.domain.repository.PalmRepository
import com.palmz.domain.repository.ScanQuotaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

enum class ScanPhase { GUIDE, PROCESSING, RESULT, ERROR }

sealed interface ScannerEvent {
    data object PaywallRequired : ScannerEvent

    /** Fired once, right after the user's first real reading — the moment a daily nudge makes sense. */
    data object RequestNotificationPermission : ScannerEvent
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val palmRepository: PalmRepository,
    private val scanQuotaRepository: ScanQuotaRepository,
    private val imagePreprocessor: ImagePreprocessor,
    private val adManager: AdManager,
    private val debugDiagnostics: DebugDiagnostics,
    @DefaultDispatcher private val cpu: CoroutineDispatcher,
) : ViewModel() {

    var phase by mutableStateOf(ScanPhase.GUIDE)
        private set

    var hand by mutableStateOf(Hand.RIGHT)
        private set

    var reading by mutableStateOf<PalmReading?>(null)
        private set

    /** The processed capture the model saw — shown behind the traced lines on the result. */
    var capturedImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /** True while the RESULT of an ad-eligible scan (2..cap) is on screen — the screen shows
     *  an interstitial on exit. */
    var showAdOnExit by mutableStateOf(false)
        private set

    /** scansUsed as of the last completed scan — for the paywall counters in the debug overlay. */
    var lastScanCount by mutableStateOf(0)
        private set

    val scansRemaining: StateFlow<Int> = scanQuotaRepository.remaining
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_FREE_SCAN_CAP)

    private val _events = Channel<ScannerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun updateHand(value: Hand) {
        hand = value
    }

    /** [rawJpeg] is the untouched CameraX capture. */
    fun capture(rawJpeg: ByteArray) {
        if (phase != ScanPhase.GUIDE) return
        viewModelScope.launch {
            if (!scanQuotaRepository.canScan.first()) {
                _events.send(ScannerEvent.PaywallRequired)
                return@launch
            }
            phase = ScanPhase.PROCESSING
            adManager.preloadInterstitial() // warm it during the (slow) reading call
            runCatching {
                val processed = imagePreprocessor.preprocess(rawJpeg)
                capturedImage = withContext(cpu) {
                    BitmapFactory.decodeByteArray(processed, 0, processed.size)?.asImageBitmap()
                }
                palmRepository.scan(hand, processed)
            }
                .onSuccess {
                    reading = it
                    phase = ScanPhase.RESULT
                    // scansUsed ticks to 1 only when this scan was actually recorded (real palm).
                    val used = scanQuotaRepository.scansUsed.first()
                    lastScanCount = used
                    showAdOnExit = interstitialEligible(used)
                    if (used == 1) _events.send(ScannerEvent.RequestNotificationPermission)
                }
                .onFailure {
                    Log.w("Palmlens", "palm scan failed", it)
                    reading = null
                    phase = ScanPhase.ERROR // ErrorPhase renders a retry button
                }
        }
    }

    fun scanAgain() {
        reading = null
        capturedImage = null
        showAdOnExit = false
        phase = ScanPhase.GUIDE
    }

    /** Debug overlay contents (spec §7) — the screen only renders these in DEBUG builds. */
    fun debugLines(): List<Pair<String, String>> = buildList {
        add("scans used" to "$lastScanCount / $DEFAULT_FREE_SCAN_CAP")
        add("to paywall" to "${(DEFAULT_FREE_SCAN_CAP - lastScanCount).coerceAtLeast(0)}")
        add("interstitial" to adManager.interstitialStatus)
        debugDiagnostics.lastPalm?.let { d ->
            add("model" to d.model)
            add("prompt" to d.promptVersion)
            add("latency" to "${d.latencyMs} ms")
            add(
                "tokens" to (
                    d.totalTokens?.let { "$it  (${d.promptTokens}p + ${d.completionTokens}c)" } ?: "n/a"
                    ),
            )
            add("finish_reason" to (d.finishReason ?: "n/a"))
        }
        reading?.let { r ->
            add("isPalm" to r.isPalm.toString())
            add("imageQuality" to r.imageQuality.name)
            r.lines.forEach { line ->
                add("· ${line.id.name.lowercase()} conf" to "${(line.confidence * 100).roundToInt()}%")
            }
        }
    }

    fun debugRawJson(): String =
        debugDiagnostics.lastPalm?.rawJson ?: "(stub reading, no model call)"
}

/** Interstitial after every scan from the 2nd through the free cap (spec §1.4 monetisation). */
internal fun interstitialEligible(scansUsed: Int, cap: Int = DEFAULT_FREE_SCAN_CAP): Boolean =
    scansUsed in 2..cap
