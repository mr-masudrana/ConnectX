package com.connectx.app.feature.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectx.app.R
import com.connectx.app.ui.components.ErrorText
import com.connectx.app.ui.components.PrimaryButton

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).imePadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        when (state.step) {
            AuthStep.EMAIL -> {
                EmailStep(
                    state = state,
                    onEmailChange = viewModel::onEmailChange,
                    onSend = viewModel::sendCode,
                )
            }
            AuthStep.CODE -> {
                CodeStep(
                    state = state,
                    onCodeChange = viewModel::onCodeChange,
                    onVerify = viewModel::verifyCode,
                    onBack = viewModel::backToEmail,
                )
            }
        }
        ErrorText(state.error?.let { stringResource(it.messageRes()) })
    }
}

@Composable
private fun EmailStep(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.email_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    PrimaryButton(
        text = stringResource(R.string.send_code),
        loading = state.loading,
        onClick = onSend,
    )
}

@Composable
private fun CodeStep(
    state: AuthUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit,
) {
    Text(text = stringResource(R.string.code_sent_to, state.email.trim()))
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = state.code,
        onValueChange = onCodeChange,
        label = { Text(stringResource(R.string.code_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    PrimaryButton(
        text = stringResource(R.string.verify),
        loading = state.loading,
        onClick = onVerify,
    )
    TextButton(onClick = onBack) {
        Text(text = stringResource(R.string.change_email))
    }
}

@StringRes
private fun AuthError.messageRes(): Int =
    when (this) {
        AuthError.INVALID_EMAIL -> R.string.error_invalid_email
        AuthError.INVALID_CODE -> R.string.error_invalid_code
        AuthError.SEND_FAILED -> R.string.error_send_failed
        AuthError.VERIFY_FAILED -> R.string.error_verify_failed
    }
