package pk.paksoft.pos.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.coroutines.CoroutineContext
import pk.paksoft.pos.core.database.dao.BootstrapDao
import pk.paksoft.pos.core.database.entity.AccountEntity
import pk.paksoft.pos.core.database.entity.BillerEntity
import pk.paksoft.pos.core.database.entity.BrandEntity
import pk.paksoft.pos.core.database.entity.CategoryEntity
import pk.paksoft.pos.core.database.entity.CouponEntity
import pk.paksoft.pos.core.database.entity.CustomerGroupEntity
import pk.paksoft.pos.core.database.entity.ShopSettingsEntity
import pk.paksoft.pos.core.database.entity.TaxEntity
import pk.paksoft.pos.core.database.entity.UnitEntity
import pk.paksoft.pos.core.database.entity.WarehouseEntity

@Database(
    entities = [
        ShopSettingsEntity::class,
        WarehouseEntity::class,
        BillerEntity::class,
        CustomerGroupEntity::class,
        CategoryEntity::class,
        BrandEntity::class,
        UnitEntity::class,
        TaxEntity::class,
        CouponEntity::class,
        AccountEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(PosDatabaseConstructor::class)
abstract class PosDatabase : RoomDatabase() {
    abstract fun bootstrapDao(): BootstrapDao
}

// Room's generated actuals implement this.
@Suppress("KotlinNoActualForExpect")
expect object PosDatabaseConstructor : RoomDatabaseConstructor<PosDatabase> {
    override fun initialize(): PosDatabase
}

/** Phase 0 tables are only a cache of server data, so a schema change just rebuilds them. */
fun buildPosDatabase(builder: RoomDatabase.Builder<PosDatabase>, ioContext: CoroutineContext): PosDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioContext)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
