package com.palmz.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmz.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { SIGN_IN, SIGN_UP }

private const val MIN_PASSWORD_LENGTH = 6

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var mode by mutableStateOf(AuthMode.SIGN_IN)
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val canSubmit: Boolean
        get() = !loading && email.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH

    fun updateEmail(value: String) {
        email = value.trim()
        error = null
    }

    fun updatePassword(value: String) {
        password = value
        error = null
    }

    fun toggleMode() {
        mode = if (mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
        error = null
    }

    fun submit(onAuthenticated: () -> Unit) {
        if (!canSubmit) return
        loading = true
        error = null
        viewModelScope.launch {
            val result = when (mode) {
                AuthMode.SIGN_IN -> authRepository.signIn(email, password)
                AuthMode.SIGN_UP -> authRepository.signUp(email, password)
            }
            loading = false
            result.onSuccess { onAuthenticated() }.onFailure { error = it.localizedMessage }
        }
    }
}
