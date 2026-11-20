package com.connectx.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.app.core.Validators
import com.connectx.app.domain.model.UsernameReservedException
import com.connectx.app.domain.model.UsernameTakenException
import com.connectx.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun onDisplayNameChange(value: String) {
        _state.update { it.copy(displayName = value.take(Validators.MAX_DISPLAY_NAME), error = null) }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value.lowercase().take(USERNAME_MAX), error = null) }
    }

    fun save() {
        val current = _state.value
        if (current.loading) return
        val validationError =
            when {
                !Validators.isValidDisplayName(current.displayName) -> ProfileError.INVALID_NAME
                !Validators.isValidUsername(current.username) -> ProfileError.INVALID_USERNAME
                else -> null
            }
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val displayName = current.displayName.trim()
            val username = Validators.normalizeUsername(current.username)
            profileRepository.saveProfile(displayName, username).fold(
                onSuccess = { _state.update { s -> s.copy(loading = false, saved = true) } },
                onFailure = { e -> _state.update { s -> s.copy(loading = false, error = e.toProfileError()) } },
            )
        }
    }

    private fun Throwable.toProfileError(): ProfileError =
        when (this) {
            is UsernameTakenException -> ProfileError.USERNAME_TAKEN
            is UsernameReservedException -> ProfileError.USERNAME_RESERVED
            else -> ProfileError.SAVE_FAILED
        }

    private companion object {
        const val USERNAME_MAX = 32
    }
}
