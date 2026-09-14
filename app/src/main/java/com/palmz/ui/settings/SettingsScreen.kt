package com.palmz.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmz.BuildConfig
import com.palmz.R
import com.palmz.ui.components.ClayCard
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.theme.Spacing

private const val WEBSITE_URL = "https://palmz-app.com/"
private const val HELP_URL = "https://palmz-app.com/faq"
private const val TERMS_URL = "https://palmz-app.com/terms"
private const val PRIVACY_POLICY_URL = "https://palmz-app.com/privacy"

@Composable
fun SettingsScreen(
    onChangeLanguage: () -> Unit,
    onDataDeleted: () -> Unit,
    onSignedOut: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val vm: SettingsViewModel = hiltViewModel()
    val activity = LocalActivity.current
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    val darkOverride by vm.darkModeOverride.collectAsStateWithLifecycle()
    fun openUrl(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    LaunchedEffect(vm.deleted) {
        if (vm.deleted) {
            onDataDeleted()
            vm.consumeDeleted()
        }
    }

    LaunchedEffect(vm.signedOut) {
        if (vm.signedOut) {
            onSignedOut()
            vm.consumeSignedOut()
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
                title = stringResource(R.string.settings_website),
                subtitle = stringResource(R.string.settings_website_desc),
                external = true,
                onClick = { openUrl(WEBSITE_URL) },
            )

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_help),
                subtitle = stringResource(R.string.settings_help_desc),
                external = true,
                onClick = { openUrl(HELP_URL) },
            )

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_terms),
                subtitle = stringResource(R.string.settings_terms_desc),
                external = true,
                onClick = { openUrl(TERMS_URL) },
            )

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_privacy_policy),
                subtitle = stringResource(R.string.settings_privacy_policy_desc),
                external = true,
                onClick = { openUrl(PRIVACY_POLICY_URL) },
            )

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_sign_out),
                subtitle = stringResource(R.string.settings_sign_out_desc),
                onClick = vm::signOut,
            )

            Spacer(Modifier.height(Spacing.space12))
            SettingRow(
                title = stringResource(R.string.settings_delete),
                subtitle = stringResource(R.string.settings_delete_desc),
                titleColor = MaterialTheme.colorScheme.error,
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

/**
 * [external] adds a small "opens in browser" affordance for links that leave the app.
 * [titleColor] lets a destructive row (delete data) tint its title without a bespoke variant.
 */
@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    external: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    ClayCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
                Spacer(Modifier.height(Spacing.space2))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (external) {
                Spacer(Modifier.width(Spacing.space8))
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
