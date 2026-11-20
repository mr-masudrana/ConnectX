package com.connectx.app.core

/** Input rules. The username rules must stay identical to the database (0001_users.sql). */
object Validators {
    const val MAX_DISPLAY_NAME = 64
    const val MIN_OTP_LENGTH = 6
    const val MAX_OTP_LENGTH = 10

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val usernameRegex = Regex("^[a-z][a-z0-9_]{4,31}$")

    fun normalizeEmail(value: String): String = value.trim().lowercase()

    fun isValidEmail(value: String): Boolean = emailRegex.matches(normalizeEmail(value))

    fun normalizeUsername(value: String): String = value.trim().lowercase()

    fun isValidUsername(value: String): Boolean {
        val name = normalizeUsername(value)
        return usernameRegex.matches(name) && !name.contains("__")
    }

    fun isValidDisplayName(value: String): Boolean = value.trim().length in 1..MAX_DISPLAY_NAME

    fun isValidOtp(value: String): Boolean =
        value.length in MIN_OTP_LENGTH..MAX_OTP_LENGTH && value.all { it.isDigit() }
}
