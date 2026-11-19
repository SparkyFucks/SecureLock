package com.securelock.app.data.security

import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Cryptographic utility for credential hashing.
 * Uses PBKDF2 with HMAC-SHA256 and salted iterations.
 * Credentials are NEVER stored in plaintext.
 */
object HashUtils {
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    fun hashCredential(credential: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(credential.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    fun verifyCredential(credential: String, salt: ByteArray, expectedHash: ByteArray): Boolean {
        val actualHash = hashCredential(credential, salt)
        if (actualHash.size != expectedHash.size) return false
        var diff = 0
        for (i in actualHash.indices) {
            diff = diff or (actualHash[i].toInt() xor expectedHash[i].toInt())
        }
        return diff == 0
    }

    fun toBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun fromBase64(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP)
}
