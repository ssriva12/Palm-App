package com.palmlens.ui.onboarding

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.domain.model.Gender
import com.palmlens.ui.components.ClayTextField
import com.palmlens.ui.components.DobPickerButton
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SecondaryButton
import com.palmlens.ui.components.TimePickerButton
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.STEP_DOB
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.STEP_GENDER
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.STEP_NAME
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.STEP_PLACE
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.STEP_TIME
import com.palmlens.ui.onboarding.OnboardingUiState.Companion.TOTAL_STEPS
import com.palmlens.ui.theme.Spacing

@Composable
fun OnboardingScreen(onExit: () -> Unit, onDone: () -> Unit) {
    val vm: OnboardingViewModel = hiltViewModel()
    val ui = vm.ui

    MysticScaffold(title = "About you", onBack = { vm.back(onExit) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(Spacing.space20),
        ) {
            LinearProgressIndicator(
                progress = { (ui.step + 1) / TOTAL_STEPS.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Spacer(Modifier.height(Spacing.space4))
            Text(
                "Step ${ui.step + 1} of $TOTAL_STEPS",
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
                        STEP_NAME -> StepShell("What should we call you?") {
                            ClayTextField(
                                value = ui.name,
                                onValueChange = vm::setName,
                                label = "Name",
                                placeholder = "Your name",
                                supportingText = "${ui.name.length}/30",
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        STEP_GENDER -> StepShell("How do you identify?") {
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

                        STEP_DOB -> StepShell("When were you born?") {
                            Text(
                                "Sets your zodiac sign and shapes every horoscope.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Spacing.space12))
                            DobPickerButton(
                                value = ui.dob,
                                label = "Date of birth",
                                onPicked = vm::setDob,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (ui.dob != null && !ui.dobValid) {
                                Spacer(Modifier.height(Spacing.space8))
                                Text(
                                    "You need to be at least 13 to use Palmlens.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        STEP_TIME -> StepShell("Do you know your birth time?") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = ui.birthTimeKnown,
                                    onCheckedChange = vm::setBirthTimeKnown,
                                )
                                Spacer(Modifier.width(Spacing.space12))
                                Text(
                                    if (ui.birthTimeKnown) "Yes, I know it" else "Not sure, skip it",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            if (ui.birthTimeKnown) {
                                Spacer(Modifier.height(Spacing.space12))
                                TimePickerButton(
                                    value = ui.birthTime,
                                    label = "Time of birth",
                                    onPicked = vm::setBirthTime,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        STEP_PLACE -> StepShell("Where were you born?") {
                            Text(
                                "Optional. Adds a little context to your readings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Spacing.space12))
                            ClayTextField(
                                value = ui.place,
                                onValueChange = vm::setPlace,
                                label = "City, country",
                                placeholder = "e.g. Lisbon, Portugal",
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
                    text = "Back",
                    modifier = Modifier.weight(1f),
                    onClick = { vm.back(onExit) },
                )
                PrimaryButton(
                    text = if (ui.isLastStep) "Finish" else "Next",
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
