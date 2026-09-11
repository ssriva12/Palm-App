package com.palmlens.ui.highlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.ads.AdBanner
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.StatTile
import com.palmlens.ui.horoscope.HoroscopeViewModel
import com.palmlens.ui.theme.Spacing

@Composable
fun HighlightsScreen(onBack: () -> Unit) {
    val vm: HoroscopeViewModel = hiltViewModel()
    val bundle by vm.bundle.collectAsStateWithLifecycle()
    val h = bundle?.highlights

    MysticScaffold(title = "Daily Highlights", onBack = onBack, bottomBar = { AdBanner() }) { pad ->
        if (h == null) {
            Column(Modifier.padding(pad).fillMaxSize()) { LoadingState("Reading the day…") }
        } else {
            Column(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.space20),
            ) {
                ClayCard(Modifier.fillMaxWidth()) {
                    Text(
                        "MOOD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(
                        h.mood,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(Spacing.space12))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space12)) {
                    StatTile("Lucky number", h.luckyNumber.toString(), Modifier.weight(1f))
                    StatTile("Lucky colour", h.luckyColor, Modifier.weight(1f))
                }

                Spacer(Modifier.height(Spacing.space12))
                ClayCard(Modifier.fillMaxWidth(), contentPadding = Spacing.space14) {
                    Text(
                        "FOCUS OF THE DAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(h.focusOfDay, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(Modifier.height(Spacing.space12))
                ClayCard(Modifier.fillMaxWidth(), contentPadding = Spacing.space14) {
                    Text(
                        "ADVICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(h.oneLineAdvice, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(Spacing.space24))
            }
        }
    }
}
