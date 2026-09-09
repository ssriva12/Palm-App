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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.domain.model.Zodiac
import com.palmlens.ui.components.FeatureTile
import com.palmlens.ui.components.GlassCard
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.StatTile
import com.palmlens.ui.navigation.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    MysticScaffold(title = "") { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "Hi ${profile.name.ifBlank { "there" }}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "${zodiac.symbol}  ${zodiac.displayName}  ·  $today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            // Today strip -> Daily Highlights
            GlassCard(onClick = { onOpen(Screen.Highlights) }) {
                Text(
                    "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Lucky no.", h.luckyNumber.toString(), Modifier.weight(1f))
                        StatTile("Lucky colour", h.luckyColor, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FeatureTile(
                emoji = "🖐",
                title = "Palm Scanner",
                subtitle = when {
                    scansRemaining <= 0 -> "You've used all your free scans"
                    else -> "$scansRemaining free scan${if (scansRemaining == 1) "" else "s"} left"
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpen(Screen.Scanner) },
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureTile("🔮", "Horoscope", "Daily · weekly · monthly", Modifier.weight(1f)) {
                    onOpen(Screen.Horoscope)
                }
                FeatureTile("✨", "Highlights", "Mood, luck, focus", Modifier.weight(1f)) {
                    onOpen(Screen.Highlights)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureTile("❤️", "Love Test", "Check your compatibility", Modifier.weight(1f)) {
                    onOpen(Screen.Love)
                }
                FeatureTile("🃏", "Tarot", "Pick three cards", Modifier.weight(1f)) {
                    onOpen(Screen.Tarot)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "For entertainment purposes only.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
