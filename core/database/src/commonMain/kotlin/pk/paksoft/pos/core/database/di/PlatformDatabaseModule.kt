package pk.paksoft.pos.core.database.di

import org.koin.core.annotation.Module

/** Provides the platform-specific `RoomDatabase.Builder<PosDatabase>`. */
@Module
expect class PlatformDatabaseModule
