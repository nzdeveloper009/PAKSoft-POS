package pk.paksoft.pos.core.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pk.paksoft.pos.core.datastore.AppPreferences

class InMemoryAppPreferences(
    activeShopId: String? = null,
    cachedSessionJson: String? = null,
    deviceId: String = "device-test",
) : AppPreferences {
    private val activeShop = MutableStateFlow(activeShopId)
    private val cachedSession = MutableStateFlow(cachedSessionJson)
    private val fixedDeviceId = deviceId

    /** When true, [setCachedSessionJson] throws like a failing DataStore and keeps the old value. */
    var failCachedSessionWrites: Boolean = false

    override val activeShopId: Flow<String?> = activeShop.asStateFlow()

    override suspend fun setActiveShopId(id: String?) {
        activeShop.value = id
    }

    override suspend fun deviceId(): String = fixedDeviceId

    override val cachedSessionJson: Flow<String?> = cachedSession.asStateFlow()

    override suspend fun setCachedSessionJson(json: String?) {
        check(!failCachedSessionWrites) { "DataStore write failed" }
        cachedSession.value = json
    }
}
