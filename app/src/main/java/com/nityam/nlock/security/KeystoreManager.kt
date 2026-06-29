package com.nityam.nlock.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages the Android Keystore key used to bind [androidx.biometric.BiometricPrompt]
 * to a [androidx.biometric.BiometricPrompt.CryptoObject].
 *
 * The key is invalidated when new biometric enrollments are added
 * ([KeyGenParameterSpec.Builder.setInvalidatedByBiometricEnrollment]),
 * preventing enrollment-based bypass attacks.
 */
internal object KeystoreManager {
    private const val KEY_ALIAS = "nlock_biometric_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    fun getCipher(): Cipher {
        val cipher = Cipher.getInstance(
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        )
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            return cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
            
            val newCipher = Cipher.getInstance(
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            )
            newCipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            return newCipher
        }
    }

    fun isKeyValid(): Boolean {
        return try {
            getCipher()
            true
        } catch (e: Exception) {
            false
        }
    }
}
