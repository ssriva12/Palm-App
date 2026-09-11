package com.palmlens.ui.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.theme.ClayShapeSmall
import com.palmlens.ui.theme.clay
import com.palmlens.ui.theme.Spacing

private data class Plan(val id: String, val title: String, val price: String, val note: String?)

private val PLANS = listOf(
    Plan("weekly", "Weekly", "placeholder", "3-day free trial"),
    Plan("monthly", "Monthly", "placeholder", null),
    Plan("yearly", "Yearly", "placeholder", "Best value"),
)

@Composable
fun SubscriptionScreen(onSkip: () -> Unit, onSubscribed: () -> Unit) {
    var selected by remember { mutableStateOf("yearly") }

    MysticScaffold(
        title = "",
        actions = {
            IconButton(onClick = onSkip) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space20),
        ) {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(30.dp),
            )
            Spacer(Modifier.height(Spacing.space10))
            Text(
                "Palmlens Premium",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                "Unlimited scans, no ads, and the daily horoscope.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.space24))
            PLANS.forEach { plan ->
                PlanRow(
                    plan = plan,
                    selected = selected == plan.id,
                    onClick = { selected = plan.id },
                )
                Spacer(Modifier.height(Spacing.space10))
            }

            Spacer(Modifier.height(Spacing.space8))
            Text(
                "Prices are placeholders in this build. The real ones come from Play Billing.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.space20))
            PrimaryButton(
                text = "Start free trial",
                modifier = Modifier.fillMaxWidth(),
                onClick = onSubscribed,
            )
            Spacer(Modifier.height(Spacing.space4))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onSkip) { Text("Maybe later") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onSubscribed) { Text("Restore purchases") }
            }
            Spacer(Modifier.height(Spacing.space16))
        }
    }
}

@Composable
private fun PlanRow(plan: Plan, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        Modifier
            .fillMaxWidth()
            .clay(
                ClayShapeSmall,
                fill = if (selected) cs.primaryContainer else cs.surface,
                pressed = pressed,
            )
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(Spacing.space16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(plan.title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            if (plan.note != null) {
                Text(
                    plan.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Text(
            plan.price,
            style = MaterialTheme.typography.titleMedium,
            color = cs.onSurfaceVariant,
        )
    }
}
