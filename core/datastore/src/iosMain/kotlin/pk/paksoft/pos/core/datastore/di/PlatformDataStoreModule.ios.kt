package pk.paksoft.pos.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.datastore.KeychainTokenStore
import pk.paksoft.pos.core.datastore.TokenStore
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformDataStoreModule {
    @OptIn(ExperimentalForeignApi::class)
    @Singleton
    fun preferencesDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        requireNotNull(dir?.path) { "Documents directory unavailable" }.plus("/pos.preferences_pb").toPath()
    }

    @Singleton
    fun tokenStore(): TokenStore = KeychainTokenStore()
}
