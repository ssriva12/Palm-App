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
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun OnboardingScreen(onExit: () -> Unit, onDone: () -> Unit) {
    val vm: OnboardingViewModel = hiltViewModel()
    val ui = vm.ui

    MysticScaffold(title = "About you", onBack = { vm.back(onExit) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(20.dp),
        ) {
            LinearProgressIndicator(
                progress = { (ui.step + 1) / TOTAL_STEPS.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Spacer(Modifier.height(4.dp))
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
                Column(Modifier.fillMaxWidth().padding(top = 28.dp)) {
                    when (step) {
                        STEP_NAME -> StepShell("What should we call you?") {
                            OutlinedTextField(
                                value = ui.name,
                                onValueChange = vm::setName,
                                label = { Text("Name") },
                                singleLine = true,
                                supportingText = { Text("${ui.name.length}/30") },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        STEP_GENDER -> StepShell("How do you identify?") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            Spacer(Modifier.height(12.dp))
                            DobPickerButton(
                                value = ui.dob,
                                onPicked = vm::setDob,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (ui.dob != null && !ui.dobValid) {
                                Spacer(Modifier.height(8.dp))
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
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (ui.birthTimeKnown) "Yes, I know it" else "Not sure — skip",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            if (ui.birthTimeKnown) {
                                Spacer(Modifier.height(12.dp))
                                TimePickerButton(
                                    value = ui.birthTime,
                                    onPicked = vm::setBirthTime,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        STEP_PLACE -> StepShell("Where were you born?") {
                            Text(
                                "Optional — adds context to your astro readings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = ui.place,
                                onValueChange = vm::setPlace,
                                label = { Text("City, country") },
                                singleLine = true,
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Spacer(Modifier.height(20.dp))
        content()
    }
}
