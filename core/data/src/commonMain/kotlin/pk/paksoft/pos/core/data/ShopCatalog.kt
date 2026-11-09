package pk.paksoft.pos.core.data

import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.Shops

/** The shops this build can connect to (dev shops only in debug builds). */
@Singleton
class ShopCatalog(@Provided config: AppConfig) {
    val shops: List<Shop> = Shops.forBuild(config.isDebug, config.localApiBaseUrl)

    fun byId(id: String?): Shop? = id?.let { wanted -> shops.firstOrNull { it.id == wanted } }
}
