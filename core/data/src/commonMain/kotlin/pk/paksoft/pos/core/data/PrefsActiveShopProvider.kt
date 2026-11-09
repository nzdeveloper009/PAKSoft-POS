package pk.paksoft.pos.core.data

import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.network.ActiveShopProvider

/** The base URL of the shop saved as active, read on every request. */
@Singleton
class PrefsActiveShopProvider(private val prefs: AppPreferences, private val catalog: ShopCatalog) : ActiveShopProvider {
    override suspend fun currentApiBaseUrl(): String? = catalog.byId(prefs.activeShopId.first())?.apiBaseUrl
}
