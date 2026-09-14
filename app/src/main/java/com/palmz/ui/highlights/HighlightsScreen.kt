package com.palmz.ui.highlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmz.R
import com.palmz.ads.AdBanner
import com.palmz.ui.components.ClayCard
import com.palmz.ui.components.LoadingState
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.components.StatTile
import com.palmz.ui.horoscope.HoroscopeViewModel
import com.palmz.ui.theme.Spacing

@Composable
fun HighlightsScreen(onBack: () -> Unit) {
    val vm: HoroscopeViewModel = hiltViewModel()
    val bundle by vm.bundle.collectAsStateWithLifecycle()
    val h = bundle?.highlights

    MysticScaffold(title = stringResource(R.string.highlights_title), onBack = onBack, bottomBar = { AdBanner() }) { pad ->
        if (h == null) {
            Column(Modifier.padding(pad).fillMaxSize()) { LoadingState(stringResource(R.string.highlights_loading)) }
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
                        stringResource(R.string.highlights_mood_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(
                        h.mood,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(Spacing.space12))
                Row(
                    Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
                ) {
                    StatTile(
                        stringResource(R.string.highlights_lucky_number_label),
                        h.luckyNumber.toString(),
                        Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatTile(stringResource(R.string.stat_lucky_colour), h.luckyColor, Modifier.weight(1f).fillMaxHeight())
                }

                Spacer(Modifier.height(Spacing.space12))
                ClayCard(Modifier.fillMaxWidth(), contentPadding = Spacing.space14) {
                    Text(
                        stringResource(R.string.highlights_focus_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(
                        h.focusOfDay,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(Spacing.space12))
                ClayCard(Modifier.fillMaxWidth(), contentPadding = Spacing.space14) {
                    Text(
                        stringResource(R.string.highlights_advice_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space4))
                    Text(
                        h.oneLineAdvice,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(Spacing.space24))
            }
        }
    }
}
