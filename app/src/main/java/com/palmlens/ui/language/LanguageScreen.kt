package com.palmlens.ui.language

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.domain.model.Language
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SelectableRow
import com.palmlens.ui.theme.Spacing

@Composable
fun LanguageScreen(fromSettings: Boolean = false, onDone: () -> Unit) {
    val vm: LanguageViewModel = hiltViewModel()

    MysticScaffold(
        title = "Choose your language",
        onBack = if (fromSettings) onDone else null,
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Text(
                "Every reading is written in the language you pick.",
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
                text = if (fromSettings) "Save" else "Continue",
                modifier = Modifier.fillMaxWidth().padding(Spacing.space16),
                onClick = { vm.commit(onDone) },
            )
        }
    }
}
