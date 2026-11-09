package pk.paksoft.pos.core.data

import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.database.dao.BootstrapDao

/** Clears under [SessionEpoch], so a refresh that started before the clear can't write after it. */
@Singleton
internal class RoomLocalDataCleaner(private val dao: BootstrapDao, private val epoch: SessionEpoch) : LocalDataCleaner {
    override suspend fun clearAll() = epoch.clear { dao.clearAll() }
}
