package com.connectx.app.feature.profile

enum class ProfileError {
    INVALID_NAME,
    INVALID_USERNAME,
    USERNAME_TAKEN,
    USERNAME_RESERVED,
    SAVE_FAILED,
}

data class ProfileUiState(
    val displayName: String = "",
    val username: String = "",
    val loading: Boolean = false,
    val saved: Boolean = false,
    val error: ProfileError? = null,
)
