package com.palmz.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.domain.repository.AuthRepository
import com.palmz.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** null until the stored profile loads, then whether onboarding is already done. */
    val onboarded: StateFlow<Boolean?> = profileRepository.profile
        .map { it.isOnboarded }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Read synchronously — Firebase Auth caches the signed-in user from disk. */
    val isSignedIn: Boolean get() = authRepository.currentUserId != null
}
