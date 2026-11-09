package pk.paksoft.pos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.newUuid

@Singleton
class DataStoreAppPreferences(private val dataStore: DataStore<Preferences>) : AppPreferences {

    override val activeShopId: Flow<String?> = dataStore.data.map { it[ACTIVE_SHOP_ID] }

    override suspend fun setActiveShopId(id: String?) = set(ACTIVE_SHOP_ID, id)

    override suspend fun deviceId(): String {
        dataStore.data.map { it[DEVICE_ID] }.first()?.let { return it }
        // edit is serialized, so concurrent first calls agree on one value.
        var result = ""
        dataStore.edit { prefs ->
            result = prefs[DEVICE_ID] ?: newUuid().also { prefs[DEVICE_ID] = it }
        }
        return result
    }

    override val cachedSessionJson: Flow<String?> = dataStore.data.map { it[CACHED_SESSION_JSON] }

    override suspend fun setCachedSessionJson(json: String?) = set(CACHED_SESSION_JSON, json)

    private suspend fun set(key: Preferences.Key<String>, value: String?) {
        dataStore.edit { if (value == null) it.remove(key) else it[key] = value }
    }

    private companion object {
        val ACTIVE_SHOP_ID = stringPreferencesKey("active_shop_id")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val CACHED_SESSION_JSON = stringPreferencesKey("cached_session_json")
    }
}
