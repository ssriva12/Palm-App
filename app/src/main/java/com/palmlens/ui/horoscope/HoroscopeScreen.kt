package com.palmlens.ui.horoscope

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.domain.model.HoroscopeEntry
import com.palmlens.ui.components.GlassCard
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold

private val TABS = listOf("Daily", "Weekly", "Monthly")

@Composable
fun HoroscopeScreen(onBack: () -> Unit) {
    val vm: HoroscopeViewModel = hiltViewModel()
    var tab by remember { mutableIntStateOf(0) }
    val bundle by vm.bundle.collectAsStateWithLifecycle()
    val zodiac by vm.zodiac.collectAsStateWithLifecycle()

    MysticScaffold(title = "${zodiac.symbol}  ${zodiac.displayName}", onBack = onBack) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            SecondaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                TABS.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }

            val currentBundle = bundle
            if (currentBundle == null) {
                LoadingState("Consulting the stars…")
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
                        .padding(20.dp),
                ) {
                    RatingRow(entry.rating)
                    Spacer(Modifier.height(12.dp))
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(
                            entry.overview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (tab == 0) {
                        Spacer(Modifier.height(12.dp))
                        FacetCard("Love", entry.love)
                        Spacer(Modifier.height(10.dp))
                        FacetCard("Career", entry.career)
                        Spacer(Modifier.height(10.dp))
                        FacetCard("Health", entry.health)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = "Rating: $rating out of 5" },
    ) {
        repeat(5) { i ->
            Text(
                if (i < rating) "★" else "☆",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun FacetCard(label: String, text: String?) {
    if (text == null) return
    GlassCard(Modifier.fillMaxWidth(), contentPadding = 14) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
