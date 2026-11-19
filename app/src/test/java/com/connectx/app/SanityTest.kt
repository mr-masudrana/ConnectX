package com.connectx.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SanityTest {
    @Test
    fun packageNameIsCorrect() {
        assertEquals("com.connectx.app", ConnectXApp::class.java.`package`?.name)
    }
}
