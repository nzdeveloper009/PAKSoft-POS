package pk.paksoft.pos.core.datastore

import kotlinx.coroutines.flow.Flow

/** Non-secret app preferences. */
interface AppPreferences {
    val activeShopId: Flow<String?>
    suspend fun setActiveShopId(id: String?)

    /** A UUID created on first call and returned unchanged afterwards. */
    suspend fun deviceId(): String

    val cachedSessionJson: Flow<String?>
    suspend fun setCachedSessionJson(json: String?)
}
