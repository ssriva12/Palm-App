package com.palmlens.ui.language

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.R
import com.palmlens.domain.model.Language
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SelectableRow
import com.palmlens.ui.theme.Spacing

@Composable
fun LanguageScreen(fromSettings: Boolean = false, onDone: () -> Unit) {
    val vm: LanguageViewModel = hiltViewModel()
    val activity = LocalContext.current as? Activity

    MysticScaffold(
        title = stringResource(R.string.language_title),
        onBack = if (fromSettings) onDone else null,
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Text(
                stringResource(R.string.language_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.space16, vertical = Spacing.space4),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.space10),
            ) {
                items(Language.entries) { lang ->
                    SelectableRow(
                        title = lang.endonym,
                        subtitle = lang.displayName,
                        selected = vm.selected == lang,
                        onClick = { vm.select(lang) },
                    )
                }
            }
            PrimaryButton(
                text = stringResource(if (fromSettings) R.string.action_save else R.string.language_continue),
                modifier = Modifier.fillMaxWidth().padding(Spacing.space16),
                onClick = {
                    vm.commit {
                        // Onboarding's own flow (fromSettings == false) moves straight on to the
                        // next screen, which will already pick up the new locale on next cold
                        // start via MainActivity.attachBaseContext; recreating here would bounce
                        // it back to Splash instead of forward. From Settings, the language is
                        // meant to apply immediately, so force attachBaseContext to re-run now.
                        if (fromSettings) activity?.recreate() else onDone()
                    }
                },
            )
        }
    }
}
