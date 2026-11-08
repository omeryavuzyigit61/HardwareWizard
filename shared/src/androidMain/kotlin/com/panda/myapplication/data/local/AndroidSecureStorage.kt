package com.panda.myapplication.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.panda.myapplication.domain.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore AES-256-GCM donanım tabanlı güvenli depolama implementasyonu.
 * Deprecated EncryptedSharedPreferences yerine doğrudan Keystore Master Key + AES-GCM kullanır.
 */
class AndroidSecureStorage(private val context: Context) : SecureStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun put(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            val base64Encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            prefs.edit().putString(key, base64Encoded).apply()
        } catch (e: Exception) {
            // Hata durumunda anahtarı sıfırla
            prefs.edit().remove(key).apply()
        }
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val encoded = prefs.getString(key, null) ?: return@withContext null
        try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH_BYTES) return@withContext null

            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH_BYTES)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES)
            System.arraycopy(combined, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherText.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainText = cipher.doFinal(cipherText)
            String(plainText, Charsets.UTF_8)
        } catch (e: Exception) {
            // Çözme başarısız olursa veriyi sil ve null dön (sessiz çökme yok)
            prefs.edit().remove(key).apply()
            null
        }
    }

    override suspend fun remove(key: String): Unit = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    companion object {
        private const val PREFS_NAME = "hw_secure_vault"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "hw_secure_master_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
