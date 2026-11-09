package pk.paksoft.pos.core.datastore

/**
 * Per-shop auth token kept in the platform secure store (Android Keystore / iOS Keychain).
 * A token that cannot be read back (e.g. the key was invalidated) reads as null.
 * [write] and [clear] throw when the secure store fails (an empty token is rejected); callers must handle it.
 */
interface TokenStore {
    suspend fun read(shopId: String): String?
    suspend fun write(shopId: String, token: String)
    suspend fun clear(shopId: String)
}
