package com.palmz.ui.language

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.data.locale.LocaleStore
import com.palmz.domain.model.Language
import com.palmz.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var selected by mutableStateOf(Language.EN)
        private set

    init {
        viewModelScope.launch {
            selected = Language.fromCode(profileRepository.profile.first().languageCode)
        }
    }

    fun select(language: Language) {
        selected = language
    }

    fun commit(onDone: () -> Unit) {
        viewModelScope.launch {
            profileRepository.update { it.copy(languageCode = selected.code) }
            // Mirrored outside DataStore so MainActivity.attachBaseContext can read it
            // synchronously, before Hilt/DataStore are available, to apply the locale on
            // every cold start.
            LocaleStore.set(context, selected.code)
            onDone()
        }
    }
}
