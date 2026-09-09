package com.palmlens.ui.scanner

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.data.image.ImagePreprocessor
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.PalmReading
import com.palmlens.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmlens.domain.repository.PalmRepository
import com.palmlens.domain.repository.ScanQuotaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanPhase { GUIDE, PROCESSING, RESULT, ERROR }

sealed interface ScannerEvent {
    data object PaywallRequired : ScannerEvent
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val palmRepository: PalmRepository,
    private val scanQuotaRepository: ScanQuotaRepository,
    private val imagePreprocessor: ImagePreprocessor,
) : ViewModel() {

    var phase by mutableStateOf(ScanPhase.GUIDE)
        private set

    var hand by mutableStateOf(Hand.RIGHT)
        private set

    var reading by mutableStateOf<PalmReading?>(null)
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
            runCatching {
                val processed = imagePreprocessor.preprocess(rawJpeg)
                palmRepository.scan(hand, processed)
            }
                .onSuccess { reading = it; phase = ScanPhase.RESULT }
                .onFailure {
                    Log.w("Palmlens", "palm scan failed", it)
                    reading = null
                    phase = ScanPhase.ERROR // proper retry UI: Phase 4
                }
        }
    }

    fun scanAgain() {
        reading = null
        phase = ScanPhase.GUIDE
    }
}
