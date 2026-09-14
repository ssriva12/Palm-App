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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.palmlens.R
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.theme.ClayShapeSmall
import com.palmlens.ui.theme.clay
import com.palmlens.ui.theme.Spacing

private data class Plan(val id: String, val title: String, val price: String, val note: String?)

@Composable
private fun plans(): List<Plan> {
    val placeholderPrice = stringResource(R.string.subscription_price_placeholder)
    return listOf(
        Plan("weekly", stringResource(R.string.subscription_plan_weekly), placeholderPrice, stringResource(R.string.subscription_trial_note)),
        Plan("monthly", stringResource(R.string.subscription_plan_monthly), placeholderPrice, null),
        Plan("yearly", stringResource(R.string.subscription_plan_yearly), placeholderPrice, stringResource(R.string.subscription_best_value_note)),
    )
}

@Composable
fun SubscriptionScreen(onSkip: () -> Unit, onSubscribed: () -> Unit) {
    var selected by remember { mutableStateOf("yearly") }

    MysticScaffold(
        title = "",
        actions = {
            IconButton(onClick = onSkip) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cd_close),
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
                stringResource(R.string.subscription_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                stringResource(R.string.subscription_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.space24))
            plans().forEach { plan ->
                PlanRow(
                    plan = plan,
                    selected = selected == plan.id,
                    onClick = { selected = plan.id },
                )
                Spacer(Modifier.height(Spacing.space10))
            }

            Spacer(Modifier.height(Spacing.space8))
            Text(
                stringResource(R.string.subscription_price_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.space20))
            PrimaryButton(
                text = stringResource(R.string.action_start_trial),
                modifier = Modifier.fillMaxWidth(),
                onClick = onSubscribed,
            )
            Spacer(Modifier.height(Spacing.space4))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onSkip) { Text(stringResource(R.string.action_maybe_later)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onSubscribed) { Text(stringResource(R.string.action_restore_purchases)) }
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
