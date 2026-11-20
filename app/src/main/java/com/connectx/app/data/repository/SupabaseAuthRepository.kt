package com.connectx.app.data.repository

import com.connectx.app.core.Validators
import com.connectx.app.domain.model.SessionState
import com.connectx.app.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val client: SupabaseClient,
) : AuthRepository {
    override val sessionState: Flow<SessionState> =
        client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> SessionState.SIGNED_IN
                is SessionStatus.NotAuthenticated -> SessionState.SIGNED_OUT
                else -> SessionState.LOADING
            }
        }

    override suspend fun sendEmailCode(email: String): Result<Unit> {
        return runCatching {
            client.auth.signInWith(OTP) {
                this.email = Validators.normalizeEmail(email)
                createUser = true
            }
        }.map { Unit }
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): Result<Unit> {
        return runCatching {
            client.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = Validators.normalizeEmail(email),
                token = code,
            )
        }.map { Unit }
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching { client.auth.signOut() }.map { Unit }
    }
}
