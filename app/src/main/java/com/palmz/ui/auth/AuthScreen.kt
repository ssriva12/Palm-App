package com.palmz.ui.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmz.R
import com.palmz.ui.components.ClayTextField
import com.palmz.ui.components.LoadingState
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.components.PrimaryButton
import com.palmz.ui.components.SecondaryButton
import com.palmz.ui.theme.ClayShapeSmall
import com.palmz.ui.theme.Spacing
import com.palmz.ui.theme.clay

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val vm: AuthViewModel = hiltViewModel()
    val activity = LocalActivity.current
    val signIn = vm.mode == AuthMode.SIGN_IN

    MysticScaffold(title = "") { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.space20),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.app_name),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(Spacing.space8))
            Text(
                stringResource(if (signIn) R.string.auth_signin_title else R.string.auth_signup_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                stringResource(R.string.auth_subtitle),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.space20))
            ClayTextField(
                value = vm.email,
                onValueChange = vm::updateEmail,
                label = stringResource(R.string.auth_email_label),
                enabled = !vm.loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.space14))
            ClayTextField(
                value = vm.password,
                onValueChange = vm::updatePassword,
                label = stringResource(R.string.auth_password_label),
                enabled = !vm.loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
            val error = vm.error
            if (error != null) {
                Spacer(Modifier.height(Spacing.space12))
                Text(
                    error,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(Spacing.space20))
            if (vm.loading) {
                LoadingState(stringResource(R.string.auth_loading))
            } else {
                PrimaryButton(
                    text = stringResource(if (signIn) R.string.auth_signin_button else R.string.auth_signup_button),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vm.canSubmit,
                    onClick = { vm.submit(onAuthenticated) },
                )
            }
            Spacer(Modifier.height(Spacing.space14))
            SecondaryButton(
                text = stringResource(if (signIn) R.string.auth_toggle_to_signup else R.string.auth_toggle_to_signin),
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading,
                onClick = vm::toggleMode,
            )
            Spacer(Modifier.height(Spacing.space20))
            Text(
                stringResource(R.string.auth_or_divider),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.space14))
            GoogleSignInButton(
                text = stringResource(R.string.auth_google_button),
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading,
                onClick = { activity?.let { vm.submitGoogle(it, onAuthenticated) } },
            )
        }
    }
}

/** Google's brand button spec: white fill, dark gray label, full-colour "G" mark. */
@Composable
private fun GoogleSignInButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier
            .heightIn(min = 52.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clay(ClayShapeSmall, fill = Color.White, pressed = pressed && enabled)
            .clickable(interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = Spacing.space24, vertical = Spacing.space14),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_google_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(Spacing.space12))
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1F1F1F))
    }
}
