package com.connectx.app.feature.auth

enum class AuthStep { EMAIL, CODE }

enum class AuthError { INVALID_EMAIL, INVALID_CODE, SEND_FAILED, VERIFY_FAILED }

data class AuthUiState(
    val step: AuthStep = AuthStep.EMAIL,
    val email: String = "",
    val code: String = "",
    val loading: Boolean = false,
    val error: AuthError? = null,
)
