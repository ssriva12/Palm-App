package com.palmlens.ui.tarot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.domain.model.TarotPosition
import com.palmlens.domain.model.TarotResult
import com.palmlens.ui.components.GlassCard
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SecondaryButton

@Composable
fun TarotScreen(onBack: () -> Unit) {
    val vm: TarotViewModel = hiltViewModel()

    MysticScaffold(title = "Tarot", onBack = onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            when (vm.phase) {
                TarotPhase.PICK -> if (vm.deck.isEmpty()) {
                    LoadingState("Shuffling the deck…")
                } else {
                    PickPhase(
                        deckSize = vm.deck.size,
                        picked = vm.picked,
                        canReveal = vm.canReveal,
                        alreadyDrewToday = vm.alreadyDrewToday,
                        onToggle = vm::toggle,
                        onReveal = vm::reveal,
                    )
                }

                TarotPhase.REVEALING -> LoadingState("Turning the cards…")

                TarotPhase.RESULT -> vm.result?.let { ResultPhase(it, onReset = vm::reset) }

                TarotPhase.ERROR -> {
                    Text(
                        "The stars are cloudy — the cards wouldn't turn. Try again.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("Back", Modifier.fillMaxWidth(), onClick = vm::reset)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PickPhase(
    deckSize: Int,
    picked: List<Int>,
    canReveal: Boolean,
    alreadyDrewToday: Boolean,
    onToggle: (Int) -> Unit,
    onReveal: () -> Unit,
) {
    Text(
        if (alreadyDrewToday) {
            "You've had your reading today. Come back tomorrow for a new spread."
        } else {
            "Clear your mind and choose three cards."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "${picked.size} / 3 chosen",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(16.dp))

    (0 until deckSize).chunked(3).forEach { row ->
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { index ->
                CardBack(
                    selected = index in picked,
                    order = picked.indexOf(index).takeIf { it >= 0 }?.plus(1),
                    modifier = Modifier.weight(1f),
                    onClick = { onToggle(index) },
                )
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }

    Spacer(Modifier.height(8.dp))
    PrimaryButton("Reveal", Modifier.fillMaxWidth(), enabled = canReveal, onClick = onReveal)
}

@Composable
private fun CardBack(
    selected: Boolean,
    order: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, label = "cardScale")
    Box(
        modifier
            .aspectRatio(0.64f)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(cs.primaryContainer, cs.surface)))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) cs.primary else cs.outlineVariant,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (order != null) "$order" else "✦", style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
    }
}

@Composable
private fun ResultPhase(result: TarotResult, onReset: () -> Unit) {
    TarotPosition.entries.forEachIndexed { i, position ->
        val card = result.cards.getOrNull(i)
        GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Text(
                position.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card?.emoji ?: "✦", fontSize = 24.sp)
                Spacer(Modifier.height(0.dp))
                Text(
                    "  ${card?.name ?: position.label}${if (card?.reversed == true) " (reversed)" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                result.textFor(position),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Text(
            "THE SPREAD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            result.overall,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    Spacer(Modifier.height(16.dp))
    SecondaryButton("New reading", Modifier.fillMaxWidth(), onClick = onReset)
    Text(
        "Cards are shuffled and drawn on your device.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        textAlign = TextAlign.Center,
    )
}
