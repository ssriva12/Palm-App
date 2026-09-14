package com.palmz.ui.love

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.domain.model.LoveResult
import com.palmz.domain.repository.LoveRepository
import com.palmz.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class LovePhase { FORM, LOADING, RESULT, ERROR }

@HiltViewModel
class LoveViewModel @Inject constructor(
    private val loveRepository: LoveRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val selfName: StateFlow<String> = profileRepository.profile
        .map { it.name.ifBlank { "You" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "You")

    var partnerName by mutableStateOf("")
        private set

    var partnerDob by mutableStateOf<LocalDate?>(null)
        private set

    var phase by mutableStateOf(LovePhase.FORM)
        private set

    var result by mutableStateOf<LoveResult?>(null)
        private set

    val canSubmit: Boolean get() = partnerName.trim().isNotEmpty()

    fun updatePartnerName(value: String) {
        partnerName = value.take(30)
    }

    fun updatePartnerDob(value: LocalDate) {
        partnerDob = value
    }

    fun calculate() {
        if (!canSubmit || phase == LovePhase.LOADING) return
        phase = LovePhase.LOADING
        viewModelScope.launch {
            runCatching {
                val profile = profileRepository.profile.first()
                loveRepository.compatibility(
                    selfName = profile.name.ifBlank { "You" },
                    partnerName = partnerName.trim(),
                    partnerDob = partnerDob,
                    locale = profile.languageCode,
                )
            }.onSuccess { result = it; phase = LovePhase.RESULT }
                .onFailure {
                    Log.w("Palmlens", "love test failed", it)
                    phase = LovePhase.ERROR
                }
        }
    }

    fun reset() {
        phase = LovePhase.FORM
        result = null
    }
}
