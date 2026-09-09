package com.palmlens.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton

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
            IconButton(
                onClick = onSkip,
                modifier = Modifier.semantics { contentDescription = "Close" },
            ) {
                Text("✕", style = MaterialTheme.typography.titleMedium)
            }
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text("✦", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Palmlens Premium",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Unlimited scans · ad-free · daily horoscope",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))
            PLANS.forEach { plan ->
                PlanRow(
                    plan = plan,
                    selected = selected == plan.id,
                    onClick = { selected = plan.id },
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Prices are placeholders in this build — the real ones come from Play Billing.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Start free trial",
                modifier = Modifier.fillMaxWidth(),
                onClick = onSubscribed,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = onSkip) { Text("Maybe later") }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = onSubscribed) { Text("Restore purchases") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlanRow(plan: Plan, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) cs.primaryContainer else cs.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) cs.primary else cs.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
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
