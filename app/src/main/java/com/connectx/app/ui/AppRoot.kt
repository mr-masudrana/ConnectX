package com.connectx.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectx.app.feature.auth.LoginScreen
import com.connectx.app.feature.chat.ChatListScreen
import com.connectx.app.feature.profile.ProfileSetupScreen

@Composable
fun AppRoot(viewModel: RootViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        RootState.Loading -> LoadingScreen()
        RootState.SignedOut -> LoginScreen()
        RootState.ProfileSetup -> ProfileSetupScreen(onDone = viewModel::loadProfile)
        RootState.LoadFailed -> LoadFailedScreen(onRetry = viewModel::loadProfile)
        is RootState.Ready -> ChatListScreen(profile = current.profile, onSignOut = viewModel::signOut)
    }
}
