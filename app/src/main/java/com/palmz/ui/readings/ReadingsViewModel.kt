package com.palmz.ui.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmz.domain.repository.ScanQuotaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    scanQuotaRepository: ScanQuotaRepository,
) : ViewModel() {

    val scansRemaining: StateFlow<Int> = scanQuotaRepository.remaining
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_FREE_SCAN_CAP)
}
