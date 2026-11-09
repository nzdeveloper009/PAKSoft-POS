package pk.paksoft.pos.core.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

/**
 * Tokens are encrypted with an AES/GCM key (`pos_token_key`) held in the Android Keystore and stored as
 * `base64(iv):base64(ciphertext)` under `token_<shopId>` in a private DataStore file.
 * If an entry cannot be decrypted (key invalidated, corrupt data) it is deleted and read as null, so the
 * user is simply asked to log in again. Token values are never logged.
 */
class KeystoreTokenStore(context: Context) : TokenStore {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        context.applicationContext.filesDir.resolve(FILE_NAME).absolutePath.toPath()
    }

    override suspend fun read(shopId: String): String? {
        val key = keyFor(shopId)
        val stored = dataStore.data.first()[key] ?: return null
        return runCatching { decrypt(stored) }.getOrNull() ?: run {
            dataStore.edit { it.remove(key) }
            null
        }
    }

    override suspend fun write(shopId: String, token: String) {
        val encrypted = encrypt(token)
        dataStore.edit { it[keyFor(shopId)] = encrypted }
    }

    override suspend fun clear(shopId: String) {
        dataStore.edit { it.remove(keyFor(shopId)) }
    }

    private fun keyFor(shopId: String) = stringPreferencesKey("token_$shopId")

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val cipherText = cipher.doFinal(plain.encodeToByteArray())
        return Base64.encode(cipher.iv) + ":" + Base64.encode(cipherText)
    }

    private fun decrypt(stored: String): String {
        val (iv, cipherText) = stored.split(":", limit = 2).map { Base64.decode(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
            .apply { init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv)) }
        return cipher.doFinal(cipherText).decodeToString()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "pos_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val FILE_NAME = "pos_tokens.preferences_pb"
    }
}
