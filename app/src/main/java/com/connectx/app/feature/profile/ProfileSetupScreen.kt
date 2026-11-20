package com.connectx.app.feature.profile

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun ProfileSetupScreen(
    onDone: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).imePadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = state.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = { Text(stringResource(R.string.display_name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text(stringResource(R.string.username_label)) },
            supportingText = { Text(stringResource(R.string.username_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth(),
        )
        ErrorText(state.error?.let { stringResource(it.messageRes()) })
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(R.string.continue_label),
            loading = state.loading,
            onClick = viewModel::save,
        )
    }
}

@StringRes
private fun ProfileError.messageRes(): Int =
    when (this) {
        ProfileError.INVALID_NAME -> R.string.error_invalid_name
        ProfileError.INVALID_USERNAME -> R.string.error_invalid_username
        ProfileError.USERNAME_TAKEN -> R.string.error_username_taken
        ProfileError.USERNAME_RESERVED -> R.string.error_username_reserved
        ProfileError.SAVE_FAILED -> R.string.error_save_failed
    }
