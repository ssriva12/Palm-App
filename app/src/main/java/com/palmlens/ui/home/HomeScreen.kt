package com.palmlens.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.R
import com.palmlens.ads.AdBanner
import com.palmlens.domain.model.Zodiac
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.FeatureTile
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.StatTile
import com.palmlens.ui.navigation.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.palmlens.ui.theme.Spacing

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    val vm: HomeViewModel = hiltViewModel()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val highlights by vm.highlights.collectAsStateWithLifecycle()
    val scansRemaining by vm.scansRemaining.collectAsStateWithLifecycle()
    val zodiac = profile.zodiac ?: Zodiac.CAPRICORN
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    }
    val openSettingsLabel = stringResource(R.string.cd_open_settings)

    MysticScaffold(
        title = "",
        bottomBar = { AdBanner() },
        actions = {
            IconButton(onClick = { onOpen(Screen.Settings) }) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = openSettingsLabel,
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
            Text(
                "Hi ${profile.name.ifBlank { "there" }}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "${zodiac.displayName}, $today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.space20))

            // Today strip -> Daily Highlights
            ClayCard(onClick = { onOpen(Screen.Highlights) }) {
                Text(
                    "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(Spacing.space6))
                val h = highlights
                if (h == null) {
                    Text(
                        "Reading the room…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        h.mood,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(Spacing.space10))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space10)) {
                        StatTile("Lucky no.", h.luckyNumber.toString(), Modifier.weight(1f))
                        StatTile("Lucky colour", h.luckyColor, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(Spacing.space20))

            FeatureTile(
                icon = Icons.Outlined.BackHand,
                title = "Palm Scanner",
                subtitle = when {
                    scansRemaining <= 0 -> "You've used all your free scans"
                    else -> "$scansRemaining free scan${if (scansRemaining == 1) "" else "s"} left"
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpen(Screen.Scanner) },
            )

            Spacer(Modifier.height(Spacing.space12))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space12)) {
                FeatureTile(Icons.Outlined.NightsStay, "Horoscope", "Daily, weekly, monthly", Modifier.weight(1f)) {
                    onOpen(Screen.Horoscope)
                }
                FeatureTile(Icons.Outlined.WbTwilight, "Highlights", "Mood, luck, focus", Modifier.weight(1f)) {
                    onOpen(Screen.Highlights)
                }
            }
            Spacer(Modifier.height(Spacing.space12))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space12)) {
                FeatureTile(Icons.Outlined.FavoriteBorder, "Love Test", "Check your compatibility", Modifier.weight(1f)) {
                    onOpen(Screen.Love)
                }
                FeatureTile(Icons.Outlined.Style, "Tarot", "Pick three cards", Modifier.weight(1f)) {
                    onOpen(Screen.Tarot)
                }
            }

            Spacer(Modifier.height(Spacing.space28))
            Text(
                stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.space20))
        }
    }
}
