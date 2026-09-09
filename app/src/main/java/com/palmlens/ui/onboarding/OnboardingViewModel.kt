package com.palmlens.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmlens.domain.model.Gender
import com.palmlens.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 0,
    val name: String = "",
    val gender: Gender? = null,
    val dob: LocalDate? = null,
    val birthTime: LocalTime? = null,
    val birthTimeKnown: Boolean = false,
    val place: String = "",
) {
    /** Spec §1.2: age ≥ 13, not in the future. */
    val dobValid: Boolean
        get() = dob?.let { it <= LocalDate.now() && Period.between(it, LocalDate.now()).years >= 13 } == true

    val canAdvance: Boolean
        get() = when (step) {
            STEP_NAME -> name.trim().length in 1..30
            STEP_GENDER -> gender != null
            STEP_DOB -> dobValid
            STEP_TIME -> !birthTimeKnown || birthTime != null
            STEP_PLACE -> true
            else -> false
        }

    val isLastStep: Boolean get() = step == TOTAL_STEPS - 1

    companion object {
        const val STEP_NAME = 0
        const val STEP_GENDER = 1
        const val STEP_DOB = 2
        const val STEP_TIME = 3
        const val STEP_PLACE = 4
        const val TOTAL_STEPS = 5
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    var ui by mutableStateOf(OnboardingUiState())
        private set

    fun setName(value: String) {
        ui = ui.copy(name = value.take(30))
    }

    fun setGender(value: Gender) {
        ui = ui.copy(gender = value)
    }

    fun setDob(value: LocalDate) {
        ui = ui.copy(dob = value)
    }

    fun setBirthTimeKnown(known: Boolean) {
        ui = ui.copy(birthTimeKnown = known, birthTime = if (known) ui.birthTime else null)
    }

    fun setBirthTime(value: LocalTime) {
        ui = ui.copy(birthTime = value)
    }

    fun setPlace(value: String) {
        ui = ui.copy(place = value)
    }

    fun back(onExit: () -> Unit) {
        if (ui.step == 0) onExit() else ui = ui.copy(step = ui.step - 1)
    }

    fun next(onFinished: () -> Unit) {
        if (!ui.canAdvance) return
        if (ui.isLastStep) {
            viewModelScope.launch {
                val s = ui
                profileRepository.update {
                    it.copy(
                        name = s.name.trim(),
                        gender = s.gender,
                        dob = s.dob,
                        birthTime = s.birthTime,
                        birthTimeKnown = s.birthTimeKnown,
                        birthPlace = s.place.trim(),
                    )
                }
                onFinished()
            }
        } else {
            ui = ui.copy(step = ui.step + 1)
        }
    }
}
