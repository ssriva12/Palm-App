package com.palmlens.ui.horoscope

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.R
import com.palmlens.ads.AdBanner
import com.palmlens.domain.model.HoroscopeEntry
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.FacetCard
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.theme.Spacing

private const val TAB_YEARLY = 3

@Composable
private fun tabs(): List<String> = listOf(
    stringResource(R.string.horoscope_tab_daily),
    stringResource(R.string.horoscope_tab_weekly),
    stringResource(R.string.horoscope_tab_monthly),
    stringResource(R.string.horoscope_tab_yearly),
)

@Composable
fun HoroscopeScreen(onBack: () -> Unit, initialTab: Int = 0) {
    val vm: HoroscopeViewModel = hiltViewModel()
    var tab by remember { mutableIntStateOf(initialTab) }
    val bundle by vm.bundle.collectAsStateWithLifecycle()
    val zodiac by vm.zodiac.collectAsStateWithLifecycle()

    MysticScaffold(
        title = "${zodiac.symbol}  ${zodiac.displayName}",
        onBack = onBack,
        bottomBar = { AdBanner() },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            SecondaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                tabs().forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }

            if (tab == TAB_YEARLY) {
                val overviews = stringArrayResource(R.array.yearly_overviews)
                val outlook = remember(zodiac, overviews.size) { YearlyOutlook.forYear(zodiac, overviews.size) }
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.space20),
                ) {
                    RatingRow(outlook.rating)
                    Spacer(Modifier.height(Spacing.space12))
                    ClayCard(Modifier.fillMaxWidth()) {
                        Text(
                            overviews[outlook.overviewIndex],
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(Spacing.space24))
                }
            } else {
                val currentBundle = bundle
                if (currentBundle == null) {
                    LoadingState(stringResource(R.string.horoscope_loading))
                } else {
                    val entry = when (tab) {
                        0 -> currentBundle.daily
                        1 -> currentBundle.weekly
                        else -> currentBundle.monthly
                    }
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.space20),
                    ) {
                        RatingRow(entry.rating)
                        Spacer(Modifier.height(Spacing.space12))
                        ClayCard(Modifier.fillMaxWidth()) {
                            Text(
                                entry.overview,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (tab == 0) {
                            Spacer(Modifier.height(Spacing.space12))
                            FacetCard(stringResource(R.string.horoscope_facet_love), entry.love)
                            Spacer(Modifier.height(Spacing.space10))
                            FacetCard(stringResource(R.string.horoscope_facet_career), entry.career)
                            Spacer(Modifier.height(Spacing.space10))
                            FacetCard(stringResource(R.string.horoscope_facet_health), entry.health)
                        }
                        Spacer(Modifier.height(Spacing.space24))
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Int) {
    val ratingDescription = stringResource(R.string.cd_rating, rating)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.space4),
        modifier = Modifier.clearAndSetSemantics { contentDescription = ratingDescription },
    ) {
        repeat(5) { i ->
            Icon(
                if (i < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
