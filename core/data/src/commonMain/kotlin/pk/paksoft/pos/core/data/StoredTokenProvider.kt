package pk.paksoft.pos.core.data

import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.datastore.TokenStore
import pk.paksoft.pos.core.network.AuthTokenProvider

/** The token of the shop that is active right now, read on every call so a shop switch takes effect at once. */
@Singleton
class StoredTokenProvider(private val prefs: AppPreferences, private val tokens: TokenStore) : AuthTokenProvider {
    override suspend fun token(): String? = prefs.activeShopId.first()?.let { tokens.read(it) }
}
