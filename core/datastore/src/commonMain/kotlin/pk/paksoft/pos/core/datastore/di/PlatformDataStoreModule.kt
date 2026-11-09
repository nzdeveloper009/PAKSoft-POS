package pk.paksoft.pos.core.datastore.di

import org.koin.core.annotation.Module

/** Provides the platform-specific `DataStore<Preferences>` and `TokenStore`. */
@Module
expect class PlatformDataStoreModule
