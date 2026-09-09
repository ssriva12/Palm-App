package com.palmlens.ui.horoscope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.domain.model.DailyBundle
import com.palmlens.domain.model.Zodiac
import com.palmlens.domain.repository.HoroscopeRepository
import com.palmlens.domain.repository.ProfileRepository
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

/** Backs both the Horoscope tabs and the Daily Highlights screen — one bundle (spec §3.4). */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    horoscopeRepository: HoroscopeRepository,
) : ViewModel() {

    val zodiac: StateFlow<Zodiac> = profileRepository.profile
        .map { it.zodiac ?: Zodiac.CAPRICORN }
        .stateIn(viewModelScope, SharingStarted.Lazily, Zodiac.CAPRICORN)

    val bundle: StateFlow<DailyBundle?> = profileRepository.profile
        .map { (it.zodiac ?: Zodiac.CAPRICORN) to it.languageCode }
        .distinctUntilChanged()
        .flatMapLatest { (z, locale) ->
            flow {
                emit(null)
                emit(horoscopeRepository.dailyBundle(z, locale))
            }.catch { e -> Log.w("Palmlens", "daily bundle failed", e); emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
