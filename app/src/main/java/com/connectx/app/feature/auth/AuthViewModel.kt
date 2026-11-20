package com.connectx.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.app.core.Validators
import com.connectx.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(Validators.MAX_OTP_LENGTH)
        _state.update { it.copy(code = digits, error = null) }
    }

    fun backToEmail() {
        _state.update { it.copy(step = AuthStep.EMAIL, code = "", error = null) }
    }

    fun sendCode() {
        val current = _state.value
        if (current.loading) return
        if (!Validators.isValidEmail(current.email)) {
            _state.update { it.copy(error = AuthError.INVALID_EMAIL) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            authRepository.sendEmailCode(current.email).fold(
                onSuccess = {
                    _state.update { s -> s.copy(loading = false, step = AuthStep.CODE, code = "") }
                },
                onFailure = {
                    _state.update { s -> s.copy(loading = false, error = AuthError.SEND_FAILED) }
                },
            )
        }
    }

    fun verifyCode() {
        val current = _state.value
        if (current.loading) return
        if (!Validators.isValidOtp(current.code)) {
            _state.update { it.copy(error = AuthError.INVALID_CODE) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            // On success the session flow changes and the app root switches screens.
            authRepository.verifyEmailCode(current.email, current.code).fold(
                onSuccess = { _state.update { s -> s.copy(loading = false) } },
                onFailure = {
                    _state.update { s -> s.copy(loading = false, error = AuthError.VERIFY_FAILED) }
                },
            )
        }
    }
}
