package com.palmlens.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmlens.R
import com.palmlens.ui.components.ClayTextField
import com.palmlens.ui.components.LoadingState
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SecondaryButton
import com.palmlens.ui.theme.Spacing

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val vm: AuthViewModel = hiltViewModel()
    val signIn = vm.mode == AuthMode.SIGN_IN

    MysticScaffold(title = stringResource(R.string.app_name)) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.space20),
        ) {
            Text(
                stringResource(if (signIn) R.string.auth_signin_title else R.string.auth_signup_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyMedium,
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
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
        }
    }
}
