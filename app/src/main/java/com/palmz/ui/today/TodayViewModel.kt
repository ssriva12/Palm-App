package com.palmz.ui.today

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.UserProfile
import com.palmz.domain.model.Zodiac
import com.palmz.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmz.domain.repository.HoroscopeRepository
import com.palmz.domain.repository.ProfileRepository
import com.palmz.domain.repository.ScanQuotaRepository
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
class TodayViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    horoscopeRepository: HoroscopeRepository,
    scanQuotaRepository: ScanQuotaRepository,
) : ViewModel() {

    val profile: StateFlow<UserProfile> = profileRepository.profile
        .stateIn(viewModelScope, SharingStarted.Lazily, UserProfile())

    val scansRemaining: StateFlow<Int> = scanQuotaRepository.remaining
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_FREE_SCAN_CAP)

    /** null while the daily bundle loads. `Lazily` (not `WhileSubscribed`) because this
     *  ViewModel is Activity-scoped: the flow would otherwise restart and re-emit null on
     *  every tab revisit, flickering the UI. A profile change still re-triggers via
     *  `flatMapLatest`. */
    val bundle: StateFlow<DailyBundle?> = profileRepository.profile
        .map { (it.zodiac ?: Zodiac.CAPRICORN) to it.languageCode }
        .distinctUntilChanged()
        .flatMapLatest { (zodiac, locale) ->
            flow {
                emit(null)
                emit(horoscopeRepository.dailyBundle(zodiac, locale))
            }.catch { e -> Log.w("Palmlens", "daily bundle failed", e); emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
