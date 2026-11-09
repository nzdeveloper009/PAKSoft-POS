package pk.paksoft.pos.core.database.di

import androidx.room.RoomDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppDispatchers
import pk.paksoft.pos.core.database.PosDatabase
import pk.paksoft.pos.core.database.buildPosDatabase
import pk.paksoft.pos.core.database.dao.BootstrapDao

@Module(includes = [PlatformDatabaseModule::class])
@Configuration
@ComponentScan("pk.paksoft.pos.core.database")
class DatabaseModule {
    @Singleton
    fun database(builder: RoomDatabase.Builder<PosDatabase>, dispatchers: AppDispatchers): PosDatabase =
        buildPosDatabase(builder, dispatchers.io)

    @Singleton
    fun bootstrapDao(db: PosDatabase): BootstrapDao = db.bootstrapDao()
}
