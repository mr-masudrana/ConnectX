package com.connectx.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.app.domain.model.SessionState
import com.connectx.app.domain.repository.AuthRepository
import com.connectx.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<RootState>(RootState.Loading)
    val state: StateFlow<RootState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionState.distinctUntilChanged().collect { session ->
                when (session) {
                    SessionState.LOADING -> _state.value = RootState.Loading
                    SessionState.SIGNED_OUT -> _state.value = RootState.SignedOut
                    SessionState.SIGNED_IN -> loadProfile()
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = RootState.Loading
            profileRepository.loadMyProfile().fold(
                onSuccess = { profile ->
                    _state.value = if (profile == null) RootState.ProfileSetup else RootState.Ready(profile)
                },
                onFailure = { _state.value = RootState.LoadFailed },
            )
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
