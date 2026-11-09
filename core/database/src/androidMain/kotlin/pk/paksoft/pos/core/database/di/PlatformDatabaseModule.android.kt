package pk.paksoft.pos.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.database.PosDatabase

@Module
actual class PlatformDatabaseModule {
    @Singleton
    fun databaseBuilder(context: Context): RoomDatabase.Builder<PosDatabase> {
        val appContext = context.applicationContext
        return Room.databaseBuilder<PosDatabase>(appContext, appContext.getDatabasePath("pos.db").absolutePath)
    }
}
