package com.connectx.app.domain.repository

import com.connectx.app.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionState: Flow<SessionState>

    suspend fun sendEmailCode(email: String): Result<Unit>

    suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): Result<Unit>

    suspend fun signOut(): Result<Unit>
}
