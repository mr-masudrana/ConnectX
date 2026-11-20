package com.connectx.app.domain.repository

import com.connectx.app.domain.model.Profile

interface ProfileRepository {
    /** Returns null when the user has not finished profile setup (no username yet). */
    suspend fun loadMyProfile(): Result<Profile?>

    /** Fails with UsernameTakenException or UsernameReservedException on username problems. */
    suspend fun saveProfile(
        displayName: String,
        username: String,
    ): Result<Unit>
}
