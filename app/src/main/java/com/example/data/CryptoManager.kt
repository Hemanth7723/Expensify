package com.example.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val KEY_ALIAS = "ExpenseTrackerSecretKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateKey()
    }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv // 12 bytes
        val encrypted = cipher.doFinal(bytes)
        
        // Pack IV (12 bytes) and encrypted payload together
        val packed = ByteArray(12 + encrypted.size)
        System.arraycopy(iv, 0, packed, 0, 12)
        System.arraycopy(encrypted, 0, packed, 12, encrypted.size)
        return packed
    }

    fun decrypt(packed: ByteArray): ByteArray {
        if (packed.size < 12) throw IllegalArgumentException("Encrypted data is too short")
        val iv = ByteArray(12)
        System.arraycopy(packed, 0, iv, 0, 12)
        
        val encryptedSize = packed.size - 12
        val encrypted = ByteArray(encryptedSize)
        System.arraycopy(packed, 12, encrypted, 0, encryptedSize)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encrypted)
    }

    fun encryptString(text: String): String {
        if (text.isEmpty()) return ""
        val packed = encrypt(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(packed, Base64.DEFAULT)
    }

    fun decryptString(encodedText: String): String {
        if (encodedText.isEmpty()) return ""
        return try {
            val packed = Base64.decode(encodedText, Base64.DEFAULT)
            val decryptedBytes = decrypt(packed)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
