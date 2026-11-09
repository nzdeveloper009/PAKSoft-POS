package pk.paksoft.pos.core.data

import kotlinx.coroutines.flow.Flow
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.model.ShopSettings

/** The shop's settings and reference data from `GET /bootstrap`, cached in Room. */
interface BootstrapRepository {
    /** The cached settings, or null before the first successful refresh. */
    val settings: Flow<ShopSettings?>

    /** Fetches bootstrap and replaces the cache; on failure the cache is kept. */
    suspend fun refresh(): AppResult<Unit>
}
