package com.palmlens.ui.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.BuildConfig
import com.palmlens.R
import com.palmlens.ui.components.ClayCard
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.theme.Spacing

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangeLanguage: () -> Unit,
    onDataDeleted: () -> Unit,
) {
    val vm: SettingsViewModel = hiltViewModel()
    val activity = LocalActivity.current
    var confirmDelete by remember { mutableStateOf(false) }
    val darkOverride by vm.darkModeOverride.collectAsStateWithLifecycle()

    LaunchedEffect(vm.deleted) {
        if (vm.deleted) {
            onDataDeleted()
            vm.consumeDeleted()
        }
    }

    MysticScaffold(title = stringResource(R.string.settings), onBack = onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.space20),
        ) {
            SettingRow(
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(R.string.settings_language_desc),
                onClick = onChangeLanguage,
            )

            Spacer(Modifier.height(Spacing.space12))
            ThemeSwitchRow(
                darkMode = darkOverride ?: isSystemInDarkTheme(),
                onToggle = vm::setDarkMode,
            )

            if (vm.privacyOptionsAvailable && activity != null) {
                Spacer(Modifier.height(Spacing.space12))
                SettingRow(
                    title = stringResource(R.string.settings_privacy),
                    subtitle = stringResource(R.string.settings_privacy_desc),
                    onClick = { vm.openPrivacyOptions(activity) },
                )
            }

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_delete),
                subtitle = stringResource(R.string.settings_delete_desc),
                onClick = { confirmDelete = true },
            )

            Spacer(Modifier.height(Spacing.space28))
            Text(
                stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = { Text(stringResource(R.string.settings_delete_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAllData()
                }) { Text(stringResource(R.string.settings_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ThemeSwitchRow(darkMode: Boolean, onToggle: (Boolean) -> Unit) {
    ClayCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_dark_mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Spacing.space2))
                Text(
                    stringResource(R.string.settings_dark_mode_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = darkMode, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, onClick: () -> Unit) {
    ClayCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.space2))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
