package com.connectx.app.data.repository

import com.connectx.app.domain.model.Profile
import com.connectx.app.domain.model.UsernameReservedException
import com.connectx.app.domain.model.UsernameTakenException
import com.connectx.app.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseProfileRepository @Inject constructor(
    private val client: SupabaseClient,
) : ProfileRepository {
    override suspend fun loadMyProfile(): Result<Profile?> {
        return runCatching {
            val userId = requireUserId()
            val result =
                client.from(USERS_TABLE).select {
                    filter { eq("id", userId) }
                }
            val row = result.decodeSingleOrNull<JsonObject>()
            val username = row?.stringOrNull("username")
            if (row == null || username == null) null else Profile(userId, username, row.stringOrNull("display_name"))
        }
    }

    override suspend fun saveProfile(
        displayName: String,
        username: String,
    ): Result<Unit> {
        return runCatching {
            val userId = requireUserId()
            client.from(USERS_TABLE).update({
                set("display_name", displayName)
                set("username", username)
            }) {
                filter { eq("id", userId) }
            }
        }.map { Unit }.recoverCatching { error -> throw mapSaveError(error) }
    }

    private fun JsonObject.stringOrNull(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

    private fun requireUserId(): String = client.auth.currentUserOrNull()?.id ?: error("Not signed in")

    private fun mapSaveError(error: Throwable): Throwable {
        val message = error.message.orEmpty()
        return when {
            message.contains("username_reserved") -> UsernameReservedException()
            message.contains("23505") || message.contains("duplicate key") -> UsernameTakenException()
            else -> error
        }
    }

    private companion object {
        const val USERS_TABLE = "users"
    }
}
