package com.palmlens.ui.tarot

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.domain.model.TarotCard
import com.palmlens.domain.model.TarotResult
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.TarotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TarotPhase { PICK, REVEALING, RESULT, ERROR }

/** Cards dealt face-down to pick from (spec: 3-card spread). */
const val DECK_SIZE = 18

@HiltViewModel
class TarotViewModel @Inject constructor(
    private val tarotRepository: TarotRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    var deck by mutableStateOf<List<TarotCard>>(emptyList())
        private set

    var picked by mutableStateOf<List<Int>>(emptyList())
        private set

    var phase by mutableStateOf(TarotPhase.PICK)
        private set

    var result by mutableStateOf<TarotResult?>(null)
        private set

    var alreadyDrewToday by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            deck = tarotRepository.deck().shuffled().take(DECK_SIZE)
            alreadyDrewToday = !tarotRepository.canDrawToday()
        }
    }

    val canReveal: Boolean get() = picked.size == 3 && !alreadyDrewToday

    fun toggle(index: Int) {
        picked = when {
            index in picked -> picked - index
            picked.size < 3 -> picked + index
            else -> picked
        }
    }

    fun reveal() {
        if (!canReveal || phase == TarotPhase.REVEALING) return
        phase = TarotPhase.REVEALING
        viewModelScope.launch {
            runCatching {
                val locale = profileRepository.profile.first().languageCode
                tarotRepository.reading(picked.map { deck[it] }, locale)
            }.onSuccess {
                result = it
                alreadyDrewToday = true
                phase = TarotPhase.RESULT
            }.onFailure {
                Log.w("Palmlens", "tarot reading failed", it)
                phase = TarotPhase.ERROR
            }
        }
    }

    fun reset() {
        picked = emptyList()
        result = null
        phase = TarotPhase.PICK
    }
}
