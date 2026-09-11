package com.palmlens.ui.settings

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.ads.AdManager
import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.data.local.PalmlensDatabase
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.ThemePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    // ponytail: a VM touching the DB directly — a "wipe everything" op doesn't warrant its
    //   own repository. Room's clearAllTables() empties every table in one call.
    private val database: PalmlensDatabase,
    private val adManager: AdManager,
    private val themePreferenceRepository: ThemePreferenceRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ViewModel() {

    val darkModeOverride: StateFlow<Boolean?> = themePreferenceRepository.darkModeOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch { themePreferenceRepository.setDarkMode(dark) }
    }

    /** UMP only asks for a "privacy options" entry in some regions (e.g. EEA). */
    val privacyOptionsAvailable: Boolean get() = adManager.privacyOptionsRequired

    /** One-shot: the screen navigates to the splash and calls [consumeDeleted]. This VM is
     *  Activity-scoped, so it would otherwise re-fire if Settings is ever reopened. */
    var deleted by mutableStateOf(false)
        private set

    fun consumeDeleted() {
        deleted = false
    }

    fun openPrivacyOptions(activity: Activity) = adManager.showPrivacyOptions(activity)

    /** Clears Room + DataStore, then signals the screen to restart at the splash. */
    fun deleteAllData() {
        viewModelScope.launch {
            withContext(io) { database.clearAllTables() }
            profileRepository.clear()
            deleted = true
        }
    }
}
