package com.connectx.app.ui

import com.connectx.app.domain.model.Profile

sealed interface RootState {
    data object Loading : RootState

    data object SignedOut : RootState

    data object ProfileSetup : RootState

    data object LoadFailed : RootState

    data class Ready(val profile: Profile) : RootState
}
