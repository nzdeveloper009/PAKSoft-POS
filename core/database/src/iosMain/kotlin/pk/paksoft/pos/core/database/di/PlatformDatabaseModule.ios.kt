package pk.paksoft.pos.core.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.database.PosDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformDatabaseModule {
    @OptIn(ExperimentalForeignApi::class)
    @Singleton
    fun databaseBuilder(): RoomDatabase.Builder<PosDatabase> {
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(dir?.path) { "Documents directory unavailable" } + "/pos.db"
        return Room.databaseBuilder<PosDatabase>(name = path)
    }
}
