package com.nityam.nlock.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and verifies PINs using PBKDF2WithHmacSHA256.
 *
 * Uses Android's built-in [SecretKeyFactory] — zero external dependencies.
 * Each PIN is hashed with a unique 16-byte random salt and [ITERATION_COUNT]
 * iterations, producing a [KEY_LENGTH_BITS]-bit derived key.
 *
 * The PIN itself is **never** stored or logged.
 */
internal class PinHashManager {

    /**
     * Hashes [pin] with a freshly generated random salt.
     *
     * @return Pair of (Base64-encoded hash, Base64-encoded salt).
     */
    internal fun hashPin(pin: String): Pair<String, String> {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = deriveKey(pin = pin, salt = salt)
        return Pair(
            Base64.encodeToString(hash, Base64.NO_WRAP),
            Base64.encodeToString(salt, Base64.NO_WRAP),
        )
    }

    /**
     * Verifies [pin] against a previously stored hash and salt.
     *
     * Uses [MessageDigest.isEqual] for constant-time comparison
     * to prevent timing side-channel attacks.
     */
    internal fun verifyPin(
        pin: String,
        storedHashBase64: String,
        storedSaltBase64: String,
    ): Boolean {
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val candidateHash = deriveKey(pin = pin, salt = storedSalt)
        return MessageDigest.isEqual(storedHash, candidateHash)
    }

    private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH_BITS,
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATION_COUNT = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
    }
}
