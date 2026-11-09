package pk.paksoft.pos.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.datastore.KeystoreTokenStore
import pk.paksoft.pos.core.datastore.TokenStore

@Module
actual class PlatformDataStoreModule {
    @Singleton
    fun preferencesDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        context.applicationContext.filesDir.resolve("pos.preferences_pb").absolutePath.toPath()
    }

    @Singleton
    fun tokenStore(context: Context): TokenStore = KeystoreTokenStore(context)
}
