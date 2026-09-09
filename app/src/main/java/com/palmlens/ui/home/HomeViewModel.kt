package com.palmlens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.domain.model.Highlights
import com.palmlens.domain.model.UserProfile
import com.palmlens.domain.model.Zodiac
import com.palmlens.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmlens.domain.repository.HoroscopeRepository
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.ScanQuotaRepository
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    horoscopeRepository: HoroscopeRepository,
    scanQuotaRepository: ScanQuotaRepository,
) : ViewModel() {

    val profile: StateFlow<UserProfile> = profileRepository.profile
        .stateIn(viewModelScope, SharingStarted.Lazily, UserProfile())

    val scansRemaining: StateFlow<Int> = scanQuotaRepository.remaining
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_FREE_SCAN_CAP)

    /**
     * null while the daily bundle loads. `Lazily` (not `WhileSubscribed`) because these
     * ViewModels are Activity-scoped: the flow would otherwise restart and re-emit null on
     * every screen revisit, flickering the UI. A profile change still re-triggers via
     * `flatMapLatest`.
     */
    val highlights: StateFlow<Highlights?> = profileRepository.profile
        .map { (it.zodiac ?: Zodiac.CAPRICORN) to it.languageCode }
        .distinctUntilChanged()
        .flatMapLatest { (zodiac, locale) ->
            flow {
                emit(null)
                emit(horoscopeRepository.dailyBundle(zodiac, locale).highlights)
            }.catch { e -> Log.w("Palmlens", "daily highlights failed", e); emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
