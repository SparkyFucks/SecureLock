package com.securelock.app

import com.securelock.app.data.security.HashUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HashUtilsTest {

    @Test
    fun generateSalt_returnsValidSalt() {
        val salt1 = HashUtils.generateSalt()
        val salt2 = HashUtils.generateSalt()

        assertEquals(16, salt1.size)
        assertEquals(16, salt2.size)
        assertFalse(salt1.contentEquals(salt2))
    }

    @Test
    fun hashCredential_isDeterministicWithSameSalt() {
        val pin = "1234"
        val salt = HashUtils.generateSalt()

        val hash1 = HashUtils.hashCredential(pin, salt)
        val hash2 = HashUtils.hashCredential(pin, salt)

        assertTrue(hash1.contentEquals(hash2))
    }

    @Test
    fun hashCredential_producesDifferentHashesForDifferentPins() {
        val salt = HashUtils.generateSalt()
        val hash1 = HashUtils.hashCredential("1234", salt)
        val hash2 = HashUtils.hashCredential("4321", salt)

        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun verifyCredential_validatesCorrectCredential() {
        val secret = "SecurePassword123"
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashCredential(secret, salt)

        val isValid = HashUtils.verifyCredential(secret, salt, hash)
        val isInvalid = HashUtils.verifyCredential("WrongPassword123", salt, hash)

        assertTrue(isValid)
        assertFalse(isInvalid)
    }

    @Test
    fun patternCredential_hashesCorrectly() {
        val pattern = "0-1-2-5-8"
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashCredential(pattern, salt)

        assertTrue(HashUtils.verifyCredential("0-1-2-5-8", salt, hash))
        assertFalse(HashUtils.verifyCredential("0-1-2-4-8", salt, hash))
    }

    @Test
    fun securityAnswer_verifiesCaseInsensitiveAndTrimmed() {
        val answer = "Bruno"
        val normalized = answer.trim().lowercase()
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashCredential(normalized, salt)

        // User enters "bruno" or " BRUNO  "
        val candidate1 = "bruno".trim().lowercase()
        val candidate2 = " BRUNO  ".trim().lowercase()
        val wrongCandidate = "Fluffy".trim().lowercase()

        assertTrue(HashUtils.verifyCredential(candidate1, salt, hash))
        assertTrue(HashUtils.verifyCredential(candidate2, salt, hash))
        assertFalse(HashUtils.verifyCredential(wrongCandidate, salt, hash))
    }
}
