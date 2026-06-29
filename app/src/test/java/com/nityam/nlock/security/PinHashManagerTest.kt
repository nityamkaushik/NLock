package com.nityam.nlock.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHashManagerTest {
    private val manager = PinHashManager()

    @Test
    fun hashPin_generatesUniqueSaltAndHash() {
        val (hash1, salt1) = manager.hashPin("1234")
        val (hash2, salt2) = manager.hashPin("1234")

        assertNotEquals(salt1, salt2)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun verifyPin_correctPin_returnsTrue() {
        val (hash, salt) = manager.hashPin("1234")
        assertTrue(manager.verifyPin("1234", hash, salt))
    }

    @Test
    fun verifyPin_incorrectPin_returnsFalse() {
        val (hash, salt) = manager.hashPin("1234")
        assertFalse(manager.verifyPin("4321", hash, salt))
    }
}
