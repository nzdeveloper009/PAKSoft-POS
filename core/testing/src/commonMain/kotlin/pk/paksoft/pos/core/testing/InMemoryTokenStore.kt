package pk.paksoft.pos.core.testing

import pk.paksoft.pos.core.datastore.TokenStore

class InMemoryTokenStore(initial: Map<String, String> = emptyMap()) : TokenStore {
    private val store = initial.toMutableMap()

    /** A snapshot of the stored tokens, keyed by shop id. */
    val tokens: Map<String, String> get() = store.toMap()

    override suspend fun read(shopId: String): String? = store[shopId]

    override suspend fun write(shopId: String, token: String) {
        store[shopId] = token
    }

    override suspend fun clear(shopId: String) {
        store.remove(shopId)
    }
}
