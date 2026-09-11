package com.palmlens.ui.tarot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.ads.AdBanner
import com.palmlens.domain.model.TarotPosition
import com.palmlens.domain.model.TarotResult
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.ErrorState
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.SecondaryButton
import com.palmlens.ui.theme.ClayShapeSmall
import com.palmlens.ui.theme.clay
import kotlinx.coroutines.delay
import com.palmlens.ui.theme.Spacing

@Composable
fun TarotScreen(onBack: () -> Unit) {
    val vm: TarotViewModel = hiltViewModel()

    // No reveal button — the reading starts on its own a beat after the 3rd card is chosen.
    // Re-keying on the pick count cancels the pending reveal if a card is deselected in time.
    LaunchedEffect(vm.picked.size, vm.phase) {
        if (vm.phase == TarotPhase.PICK && vm.canReveal) {
            delay(450)
            vm.reveal()
        }
    }

    MysticScaffold(title = "Tarot", onBack = onBack, bottomBar = { AdBanner() }) { pad ->
        val screen = Modifier
            .padding(pad)
            .fillMaxSize()
            .padding(horizontal = Spacing.space20)

        when (vm.phase) {
            TarotPhase.PICK ->
                if (vm.deck.isEmpty()) {
                    Box(screen, contentAlignment = Alignment.Center) {
                        LoadingState("Shuffling the deck…")
                    }
                } else {
                    PickPhase(
                        modifier = screen,
                        deckSize = vm.deck.size,
                        picked = vm.picked,
                        alreadyDrewToday = vm.alreadyDrewToday,
                        onToggle = vm::toggle,
                    )
                }

            TarotPhase.REVEALING -> Box(screen, contentAlignment = Alignment.Center) {
                LoadingState("Turning the cards…")
            }

            TarotPhase.RESULT -> Column(screen.verticalScroll(rememberScrollState())) {
                vm.result?.let { ResultPhase(it, onReset = vm::reset) }
                Spacer(Modifier.height(Spacing.space24))
            }

            TarotPhase.ERROR -> Box(screen, contentAlignment = Alignment.Center) {
                ErrorState(
                    onRetry = vm::reveal,
                    message = "The sky is overcast and the cards wouldn't turn. Try again.",
                )
            }
        }
    }
}

private val CARD_HEIGHT = 132.dp

@Composable
private fun PickPhase(
    modifier: Modifier,
    deckSize: Int,
    picked: List<Int>,
    alreadyDrewToday: Boolean,
    onToggle: (Int) -> Unit,
) {
    if (alreadyDrewToday) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "You've had your reading today. Come back tomorrow for a new spread.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(modifier) {
        Text(
            "Clear your mind and choose three cards. The reading begins on its own.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.space4))
        Text(
            "${picked.size} / 3 chosen",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.space16))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
            verticalArrangement = Arrangement.spacedBy(Spacing.space12),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(deckSize) { index ->
                CardBack(
                    selected = index in picked,
                    order = picked.indexOf(index).takeIf { it >= 0 }?.plus(1),
                    onClick = { onToggle(index) },
                )
            }
        }
    }
}

@Composable
private fun CardBack(
    selected: Boolean,
    order: Int?,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val scale by animateFloatAsState(if (selected) 1.05f else 1f, label = "cardScale")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .scale(scale)
            .clay(
                ClayShapeSmall,
                fill = if (selected) cs.primaryContainer else cs.surface,
                pressed = pressed && !selected,
            )
            .clickable(interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (order != null) {
            Text(
                "$order",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onPrimaryContainer,
            )
        } else {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ResultPhase(result: TarotResult, onReset: () -> Unit) {
    TarotPosition.entries.forEachIndexed { i, position ->
        val card = result.cards.getOrNull(i)
        ClayCard(Modifier.fillMaxWidth().padding(bottom = Spacing.space12)) {
            Text(
                position.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(Spacing.space6))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card?.emoji ?: "✦", fontSize = 24.sp)
                Spacer(Modifier.height(Spacing.none))
                Text(
                    "  ${card?.name ?: position.label}${if (card?.reversed == true) " (reversed)" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(Spacing.space8))
            Text(
                result.textFor(position),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    ClayCard(Modifier.fillMaxWidth()) {
        Text(
            "THE SPREAD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(Spacing.space6))
        Text(
            result.overall,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    Spacer(Modifier.height(Spacing.space16))
    SecondaryButton("New reading", Modifier.fillMaxWidth(), onClick = onReset)
    Text(
        "Cards are shuffled and drawn on your device.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.space8),
        textAlign = TextAlign.Center,
    )
}
