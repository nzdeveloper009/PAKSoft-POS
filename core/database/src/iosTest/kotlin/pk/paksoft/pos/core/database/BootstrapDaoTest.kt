package pk.paksoft.pos.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.database.entity.ShopSettingsEntity
import pk.paksoft.pos.core.database.entity.WarehouseEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BootstrapDaoTest {
    private fun db() = Room.inMemoryDatabaseBuilder<PosDatabase>().setDriver(BundledSQLiteDriver()).build()

    private fun settings(title: String) = ShopSettingsEntity(
        title = title,
        logoUrl = null,
        developedBy = null,
        dateFormat = "d-m-Y",
        invoiceFormat = "default",
        gstState = null,
        timezone = "Asia/Karachi",
        currencyId = 1,
        currencyCode = "PKR",
        currencyName = "Pakistani Rupee",
        exchangeRate = 1.0,
        currencyPosition = "prefix",
        featureLedger = true,
        featurePreviousBalance = false,
        posCustomerId = null,
        posWarehouseId = 1,
        posBillerId = null,
        posPageSize = 20,
        posKeyboardActive = true,
    )

    private fun warehouse(id: Long) = WarehouseEntity(
        id = id,
        name = "Warehouse $id",
        phone = null,
        email = null,
        address = null,
        isActive = true,
    )

    private suspend fun PosDatabase.fill(title: String, vararg warehouseIds: Long) =
        bootstrapDao().replaceAll(
            settings(title),
            warehouseIds.map(::warehouse),
            emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
        )

    @Test fun `replaceAll stores settings and lists`() = runTest {
        val db = db()
        db.fill("SMF", 1, 2)
        val dao = db.bootstrapDao()
        assertEquals("SMF", dao.observeSettings().first()?.title)
        assertEquals(listOf(1L, 2L), dao.observeWarehouses().first().map { it.id })
        assertEquals(3, dao.countAll())
        db.close()
    }

    @Test fun `replaceAll replaces previous rows`() = runTest {
        val db = db()
        db.fill("SMF", 1, 2)
        db.fill("BSF", 3)
        val dao = db.bootstrapDao()
        assertEquals("BSF", dao.observeSettings().first()?.title)
        assertEquals(listOf(3L), dao.observeWarehouses().first().map { it.id })
        assertEquals(2, dao.countAll())
        db.close()
    }

    @Test fun `clearAll empties every table`() = runTest {
        val db = db()
        db.fill("SMF", 1, 2)
        val dao = db.bootstrapDao()
        dao.clearAll()
        assertEquals(0, dao.countAll())
        assertNull(dao.observeSettings().first())
        db.close()
    }
}
