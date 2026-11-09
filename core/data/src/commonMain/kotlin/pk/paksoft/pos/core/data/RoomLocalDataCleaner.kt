package pk.paksoft.pos.core.data

import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.database.dao.BootstrapDao

@Singleton
internal class RoomLocalDataCleaner(private val dao: BootstrapDao) : LocalDataCleaner {
    override suspend fun clearAll() = dao.clearAll()
}
