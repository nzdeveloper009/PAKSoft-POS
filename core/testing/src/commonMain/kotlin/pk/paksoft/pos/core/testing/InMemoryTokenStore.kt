package pk.paksoft.pos.core.testing

import pk.paksoft.pos.core.datastore.TokenStore

class InMemoryTokenStore(initial: Map<String, String> = emptyMap()) : TokenStore {
    private val store = initial.toMutableMap()

    /** When true, [write] throws like a failing secure store and stores nothing. */
    var failWrites: Boolean = false

    /** When true, [clear] throws like a failing secure store and the token stays. */
    var failClears: Boolean = false

    /** A snapshot of the stored tokens, keyed by shop id. */
    val tokens: Map<String, String> get() = store.toMap()

    override suspend fun read(shopId: String): String? = store[shopId]

    override suspend fun write(shopId: String, token: String) {
        check(!failWrites) { "Token store write failed" }
        store[shopId] = token
    }

    override suspend fun clear(shopId: String) {
        check(!failClears) { "Token store delete failed" }
        store.remove(shopId)
    }
}
