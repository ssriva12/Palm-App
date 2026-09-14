package com.palmlens.ui.readings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.R
import com.palmlens.ads.AdBanner
import com.palmlens.ui.components.FeatureTile
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.navigation.Screen
import com.palmlens.ui.theme.Spacing

@Composable
fun ReadingsScreen(onOpen: (Screen) -> Unit) {
    val vm: ReadingsViewModel = hiltViewModel()
    val scansRemaining by vm.scansRemaining.collectAsStateWithLifecycle()

    MysticScaffold(
        title = stringResource(R.string.nav_readings),
        bottomBar = { AdBanner() },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space20),
        ) {
            Spacer(Modifier.height(Spacing.space12))
            FeatureTile(
                icon = Icons.Outlined.BackHand,
                title = stringResource(R.string.feature_palm_scanner_title),
                subtitle = if (scansRemaining <= 0) {
                    stringResource(R.string.home_scans_used_up)
                } else {
                    pluralStringResource(R.plurals.free_scans_remaining, scansRemaining, scansRemaining)
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpen(Screen.Scanner) },
            )

            Spacer(Modifier.height(Spacing.space12))
            Row(
                Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
            ) {
                FeatureTile(
                    Icons.Outlined.NightsStay,
                    stringResource(R.string.feature_horoscope_title),
                    stringResource(R.string.feature_horoscope_subtitle),
                    Modifier.weight(1f).fillMaxHeight(),
                ) {
                    onOpen(Screen.Horoscope())
                }
                FeatureTile(
                    Icons.Outlined.Style,
                    stringResource(R.string.feature_tarot_title),
                    stringResource(R.string.feature_tarot_subtitle),
                    Modifier.weight(1f).fillMaxHeight(),
                ) {
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
