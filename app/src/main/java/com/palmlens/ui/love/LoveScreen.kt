package com.palmlens.ui.love

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.ads.AdBanner
import com.palmlens.domain.model.LoveResult
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.ClayTextField
import com.palmlens.ui.components.DobPickerButton
import com.palmlens.ui.components.ErrorState
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SecondaryButton
import com.palmlens.ui.theme.Spacing

@Composable
fun LoveScreen(onBack: () -> Unit) {
    val vm: LoveViewModel = hiltViewModel()
    val selfName by vm.selfName.collectAsStateWithLifecycle()

    MysticScaffold(title = "Love Test", onBack = onBack, bottomBar = { AdBanner() }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.space20),
        ) {
            when (vm.phase) {
                LovePhase.FORM -> {
                    Text(
                        "How do you and someone match up?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.space16))
                    ClayTextField(
                        value = selfName,
                        onValueChange = {},
                        label = "You",
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space14))
                    ClayTextField(
                        value = vm.partnerName,
                        onValueChange = vm::updatePartnerName,
                        label = "Their name",
                        placeholder = "First name",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space14))
                    DobPickerButton(
                        value = vm.partnerDob,
                        label = "Their date of birth (optional)",
                        onPicked = vm::updatePartnerDob,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space20))
                    PrimaryButton(
                        "Check compatibility",
                        Modifier.fillMaxWidth(),
                        enabled = vm.canSubmit,
                        onClick = vm::calculate,
                    )
                }

                LovePhase.LOADING -> LoadingState("Reading between you two…")

                LovePhase.RESULT -> vm.result?.let { result ->
                    ResultBody(selfName, vm.partnerName, result, onReset = vm::reset)
                }

                LovePhase.ERROR -> ErrorState(
                    onRetry = vm::calculate,
                    message = "The sky is overcast and this pairing wouldn't read. Try again.",
                )
            }
            Spacer(Modifier.height(Spacing.space24))
        }
    }
}

@Composable
private fun ResultBody(selfName: String, partnerName: String, result: LoveResult, onReset: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ScoreRing(result.score)
    }
    Spacer(Modifier.height(Spacing.space6))
    Text(
        "$selfName  and  ${partnerName.ifBlank { "them" }}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(Spacing.space16))
    ClayCard(Modifier.fillMaxWidth()) {
        Text(result.verdict, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }

    Spacer(Modifier.height(Spacing.space16))
    Text("STRENGTHS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(Spacing.space6))
    result.strengths.forEach { Bullet(it) }

    Spacer(Modifier.height(Spacing.space12))
    Text("WORTH WATCHING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    Spacer(Modifier.height(Spacing.space6))
    result.challenges.forEach { Bullet(it) }

    Spacer(Modifier.height(Spacing.space20))
    SecondaryButton("Try another pairing", Modifier.fillMaxWidth(), onClick = onReset)
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.padding(vertical = Spacing.space4)) {
        Text("•  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ScoreRing(score: Int) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(150.dp)
            .clearAndSetSemantics { contentDescription = "Compatibility score: $score percent" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            drawArc(
                color = cs.outlineVariant,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = cs.secondary,
                startAngle = -90f,
                sweepAngle = 360f * (score / 100f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            "$score%",
            style = MaterialTheme.typography.headlineSmall,
            color = cs.onBackground,
            fontWeight = FontWeight.Bold,
        )
    }
}
