package com.connectx.app.feature.auth

import com.connectx.app.domain.model.SessionState
import com.connectx.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class FakeAuthRepository : AuthRepository {
    var sendResult: Result<Unit> = Result.success(Unit)
    var verifyResult: Result<Unit> = Result.success(Unit)

    override val sessionState: Flow<SessionState> = flowOf(SessionState.SIGNED_OUT)

    override suspend fun sendEmailCode(email: String): Result<Unit> = sendResult

    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): Result<Unit> = verifyResult

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val repository = FakeAuthRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun invalidEmailShowsError() {
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChange("nope")
        viewModel.sendCode()
        assertEquals(AuthError.INVALID_EMAIL, viewModel.state.value.error)
        assertEquals(AuthStep.EMAIL, viewModel.state.value.step)
    }

    @Test
    fun validEmailMovesToCodeStep() {
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChange("user@example.com")
        viewModel.sendCode()
        assertEquals(AuthStep.CODE, viewModel.state.value.step)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun sendFailureStaysOnEmailStep() {
        repository.sendResult = Result.failure(IllegalStateException("network"))
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChange("user@example.com")
        viewModel.sendCode()
        assertEquals(AuthStep.EMAIL, viewModel.state.value.step)
        assertEquals(AuthError.SEND_FAILED, viewModel.state.value.error)
    }

    @Test
    fun shortCodeShowsError() {
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChange("user@example.com")
        viewModel.sendCode()
        viewModel.onCodeChange("12")
        viewModel.verifyCode()
        assertEquals(AuthError.INVALID_CODE, viewModel.state.value.error)
    }

    @Test
    fun wrongCodeShowsVerifyError() {
        repository.verifyResult = Result.failure(IllegalStateException("bad code"))
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChange("user@example.com")
        viewModel.sendCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()
        assertEquals(AuthError.VERIFY_FAILED, viewModel.state.value.error)
    }
}
