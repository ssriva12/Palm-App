package com.palmz.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmz.R
import com.palmz.domain.model.Gender
import com.palmz.ui.components.ClayTextField
import com.palmz.ui.components.DobPickerButton
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.components.PrimaryButton
import com.palmz.ui.components.SecondaryButton
import com.palmz.ui.components.TimePickerButton
import com.palmz.ui.onboarding.OnboardingUiState.Companion.STEP_DOB
import com.palmz.ui.onboarding.OnboardingUiState.Companion.STEP_GENDER
import com.palmz.ui.onboarding.OnboardingUiState.Companion.STEP_NAME
import com.palmz.ui.onboarding.OnboardingUiState.Companion.STEP_PLACE
import com.palmz.ui.onboarding.OnboardingUiState.Companion.STEP_TIME
import com.palmz.ui.onboarding.OnboardingUiState.Companion.TOTAL_STEPS
import com.palmz.ui.theme.Spacing

@Composable
fun OnboardingScreen(onExit: () -> Unit, onDone: () -> Unit) {
    val vm: OnboardingViewModel = hiltViewModel()
    val ui = vm.ui

    MysticScaffold(title = stringResource(R.string.onboarding_title), onBack = { vm.back(onExit) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(Spacing.space20),
        ) {
            LinearProgressIndicator(
                progress = { (ui.step + 1) / TOTAL_STEPS.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Spacer(Modifier.height(Spacing.space4))
            Text(
                stringResource(R.string.onboarding_step_indicator, ui.step + 1, TOTAL_STEPS),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedContent(
                targetState = ui.step,
                transitionSpec = {
                    val forward = targetState > initialState
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally { it * dir } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it * dir } + fadeOut()) using SizeTransform(clip = false)
                },
                label = "onboardingStep",
                modifier = Modifier.weight(1f),
            ) { step ->
                Column(Modifier.fillMaxWidth().padding(top = Spacing.space28)) {
                    when (step) {
                        STEP_NAME -> StepShell(stringResource(R.string.onboarding_step_name_question)) {
                            ClayTextField(
                                value = ui.name,
                                onValueChange = vm::setName,
                                label = stringResource(R.string.onboarding_name_label),
                                placeholder = stringResource(R.string.onboarding_name_placeholder),
                                supportingText = stringResource(R.string.onboarding_name_char_count, ui.name.length),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        STEP_GENDER -> StepShell(stringResource(R.string.onboarding_step_gender_question)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space10)) {
                                Gender.entries.forEach { g ->
                                    FilterChip(
                                        selected = ui.gender == g,
                                        onClick = { vm.setGender(g) },
                                        label = { Text(g.label) },
                                    )
                                }
                            }
                        }

                        STEP_DOB -> StepShell(stringResource(R.string.onboarding_step_dob_question)) {
                            Text(
                                stringResource(R.string.onboarding_dob_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Spacing.space12))
                            DobPickerButton(
                                value = ui.dob,
                                label = stringResource(R.string.onboarding_dob_label),
                                onPicked = vm::setDob,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (ui.dob != null && !ui.dobValid) {
                                Spacer(Modifier.height(Spacing.space8))
                                Text(
                                    stringResource(R.string.onboarding_age_error),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        STEP_TIME -> StepShell(stringResource(R.string.onboarding_step_time_question)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = ui.birthTimeKnown,
                                    onCheckedChange = vm::setBirthTimeKnown,
                                )
                                Spacer(Modifier.width(Spacing.space12))
                                Text(
                                    stringResource(
                                        if (ui.birthTimeKnown) R.string.onboarding_time_known else R.string.onboarding_time_unknown,
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            if (ui.birthTimeKnown) {
                                Spacer(Modifier.height(Spacing.space12))
                                TimePickerButton(
                                    value = ui.birthTime,
                                    label = stringResource(R.string.time_of_birth_label),
                                    onPicked = vm::setBirthTime,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        STEP_PLACE -> StepShell(stringResource(R.string.onboarding_step_place_question)) {
                            Text(
                                stringResource(R.string.onboarding_place_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Spacing.space12))
                            ClayTextField(
                                value = ui.place,
                                onValueChange = vm::setPlace,
                                label = stringResource(R.string.onboarding_place_label),
                                placeholder = stringResource(R.string.onboarding_place_placeholder),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space12)) {
                SecondaryButton(
                    text = stringResource(R.string.action_back),
                    modifier = Modifier.weight(1f),
                    onClick = { vm.back(onExit) },
                )
                PrimaryButton(
                    text = stringResource(if (ui.isLastStep) R.string.action_finish else R.string.action_next),
                    modifier = Modifier.weight(1f),
                    enabled = ui.canAdvance,
                    onClick = { vm.next(onDone) },
                )
            }
        }
    }
}

@Composable
private fun StepShell(question: String, content: @Composable () -> Unit) {
    Column {
        Text(
            question,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Spacing.space20))
        content()
    }
}
