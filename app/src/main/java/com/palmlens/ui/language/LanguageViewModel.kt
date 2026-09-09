package com.palmlens.ui.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.domain.model.Language
import com.palmlens.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
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
            onDone()
        }
    }
}
