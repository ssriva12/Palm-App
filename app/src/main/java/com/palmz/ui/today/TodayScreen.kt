package com.palmz.ui.today

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmz.R
import com.palmz.ads.AdBanner
import com.palmz.domain.model.Zodiac
import com.palmz.ui.components.ClayCard
import com.palmz.ui.components.FacetCard
import com.palmz.ui.components.FeatureTile
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.components.SectionHeader
import com.palmz.ui.components.StatTile
import com.palmz.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(onOpenHighlights: () -> Unit, onOpenHoroscope: (Int) -> Unit, onOpenScanner: () -> Unit) {
    val vm: TodayViewModel = hiltViewModel()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val bundle by vm.bundle.collectAsStateWithLifecycle()
    val scansRemaining by vm.scansRemaining.collectAsStateWithLifecycle()
    val zodiac = profile.zodiac ?: Zodiac.CAPRICORN
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

    MysticScaffold(
        title = "",
        bottomBar = { AdBanner() },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            val highlights = bundle?.highlights

            // Greeting scrolls away with the rest of the page — no pinned header for it.
            Column(Modifier.padding(horizontal = Spacing.space20)) {
                Text(
                    stringResource(R.string.home_greeting, profile.name.ifBlank { stringResource(R.string.home_default_name) }),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                Text(
                    stringResource(R.string.home_greeting_subtitle, zodiac.displayName, today),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(Spacing.space20))

            // Today strip -> Daily Highlights, with the three lucky stats and quick pills
            // to jump straight to the Weekly/Monthly/Yearly horoscope.
            ClayCard(Modifier.padding(horizontal = Spacing.space20), onClick = onOpenHighlights) {
                Text(
                    stringResource(R.string.home_today_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.space6))
                if (highlights == null) {
                    Text(
                        stringResource(R.string.home_loading_highlights),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        highlights.mood,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.space10))
                    Row(
                        Modifier.height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.space10),
                    ) {
                        LuckyNumberTile(
                            stringResource(R.string.home_lucky_number_short),
                            highlights.luckyNumber.toString(),
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                        StatTile(
                            stringResource(R.string.stat_lucky_colour),
                            highlights.luckyColor,
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                        StatTile(
                            stringResource(R.string.today_lucky_time_label),
                            DailyLuck.luckyTime(zodiac).format(timeFormatter),
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    Spacer(Modifier.height(Spacing.space12))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space8)) {
                        HoroscopePill(stringResource(R.string.horoscope_tab_weekly)) { onOpenHoroscope(1) }
                        HoroscopePill(stringResource(R.string.horoscope_tab_monthly)) { onOpenHoroscope(2) }
                        HoroscopePill(stringResource(R.string.horoscope_tab_yearly)) { onOpenHoroscope(3) }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.space24))
            SectionHeader(stringResource(R.string.today_insights_title), Modifier.padding(horizontal = Spacing.space20))
            Spacer(Modifier.height(Spacing.space12))
            val daily = bundle?.daily
            // Full-bleed: edge inset comes from the leading/trailing spacers below, not from
            // padding on this Row, so the scroll can carry a card's content to the true screen
            // edge instead of stopping short inside a padded parent.
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
            ) {
                Spacer(Modifier.width(Spacing.space8))
                FacetCard(stringResource(R.string.horoscope_facet_love), daily?.love, Modifier.width(240.dp).fillMaxHeight())
                FacetCard(stringResource(R.string.horoscope_facet_career), daily?.career, Modifier.width(240.dp).fillMaxHeight())
                FacetCard(stringResource(R.string.horoscope_facet_health), daily?.health, Modifier.width(240.dp).fillMaxHeight())
                Spacer(Modifier.width(Spacing.space8))
            }

            Spacer(Modifier.height(Spacing.space24))
            SectionHeader(stringResource(R.string.feature_palm_scanner_title), Modifier.padding(horizontal = Spacing.space20))
            Spacer(Modifier.height(Spacing.space12))
            FeatureTile(
                icon = Icons.Outlined.BackHand,
                title = stringResource(R.string.feature_palm_scanner_title),
                subtitle = if (scansRemaining <= 0) {
                    stringResource(R.string.home_scans_used_up)
                } else {
                    pluralStringResource(R.plurals.free_scans_remaining, scansRemaining, scansRemaining)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.space20),
                onClick = onOpenScanner,
            )

            Spacer(Modifier.height(Spacing.space24))
            SectionHeader(stringResource(R.string.today_matches_title), Modifier.padding(horizontal = Spacing.space20))
            Spacer(Modifier.height(Spacing.space12))
            Row(
                Modifier.padding(horizontal = Spacing.space20).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(Spacing.space10),
            ) {
                MatchTile(
                    Icons.Outlined.FavoriteBorder,
                    stringResource(R.string.horoscope_facet_love),
                    DailyLuck.matchPercent(zodiac, DailyLuck.MatchCategory.LOVE),
                    Modifier.weight(1f).fillMaxHeight(),
                )
                MatchTile(
                    Icons.Outlined.WorkOutline,
                    stringResource(R.string.horoscope_facet_career),
                    DailyLuck.matchPercent(zodiac, DailyLuck.MatchCategory.CAREER),
                    Modifier.weight(1f).fillMaxHeight(),
                )
                MatchTile(
                    Icons.Outlined.Groups,
                    stringResource(R.string.today_matches_friendship),
                    DailyLuck.matchPercent(zodiac, DailyLuck.MatchCategory.FRIENDSHIP),
                    Modifier.weight(1f).fillMaxHeight(),
                )
            }

            Spacer(Modifier.height(Spacing.space28))
            Text(
                stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.space20),
            )
        }
    }
}

/**
 * Unlike [StatTile] (label + value both top-aligned), the number here fills whatever
 * height the row's equal-height siblings (lucky colour/time, which often wrap to two
 * lines) hand it, so a short single digit doesn't just sit at the top with dead space
 * below it.
 */
@Composable
private fun LuckyNumberTile(label: String, value: String, modifier: Modifier = Modifier) {
    ClayCard(modifier = modifier, contentPadding = Spacing.space14) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HoroscopePill(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = AssistChipDefaults.assistChipElevation(disabledElevation = 6.dp),
        border = null,
    )
}

@Composable
private fun MatchTile(icon: ImageVector, label: String, percent: Int, modifier: Modifier = Modifier) {
    ClayCard(modifier = modifier, contentPadding = Spacing.space14) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(Spacing.space4))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "$percent%",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
