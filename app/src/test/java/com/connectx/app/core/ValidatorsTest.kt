package com.connectx.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test
    fun acceptsNormalEmails() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("  User@Example.com "))
    }

    @Test
    fun rejectsBadEmails() {
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("user"))
        assertFalse(Validators.isValidEmail("user@"))
        assertFalse(Validators.isValidEmail("user@example"))
    }

    @Test
    fun acceptsValidUsernamesAndNormalizesCase() {
        assertTrue(Validators.isValidUsername("rahim_01"))
        assertTrue(Validators.isValidUsername("Rahim_01"))
        assertEquals("rahim_01", Validators.normalizeUsername(" Rahim_01 "))
    }

    @Test
    fun rejectsInvalidUsernames() {
        assertFalse(Validators.isValidUsername("abcd"))
        assertFalse(Validators.isValidUsername("1abcde"))
        assertFalse(Validators.isValidUsername("ab__cde"))
        assertFalse(Validators.isValidUsername("ab-cde"))
        assertFalse(Validators.isValidUsername("a".repeat(33)))
    }

    @Test
    fun validatesOtpCodes() {
        assertTrue(Validators.isValidOtp("123456"))
        assertFalse(Validators.isValidOtp("12345"))
        assertFalse(Validators.isValidOtp("12a456"))
    }

    @Test
    fun validatesDisplayName() {
        assertTrue(Validators.isValidDisplayName("Rahim"))
        assertFalse(Validators.isValidDisplayName("   "))
        assertFalse(Validators.isValidDisplayName("x".repeat(65)))
    }
}
