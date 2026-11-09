package pk.paksoft.pos.core.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.database.PosDatabase
import pk.paksoft.pos.core.testing.FakeBootstrapApi
import pk.paksoft.pos.core.testing.TestData

class DefaultBootstrapRepositoryTest {
    private val db = Room.inMemoryDatabaseBuilder<PosDatabase>().setDriver(BundledSQLiteDriver()).build()
    private val api = FakeBootstrapApi(AppResult.Success(TestData.bootstrapDto("SMF")))
    private val repo = DefaultBootstrapRepository(api, db.bootstrapDao())

    @AfterTest
    fun closeDb() {
        db.close()
    }

    @Test
    fun `refresh stores data and settings flow emits it`() = runTest {
        assertNull(repo.settings.first())

        assertEquals(AppResult.Success(Unit), repo.refresh())

        assertEquals("SMF", repo.settings.first()?.title)
        // One settings row plus one row in each of the nine reference tables.
        assertEquals(10, db.bootstrapDao().countAll())
    }

    @Test
    fun `failed refresh keeps the cached data`() = runTest {
        repo.refresh()
        api.result = AppResult.Failure(AppError.Offline)

        assertEquals(AppResult.Failure(AppError.Offline), repo.refresh())

        assertEquals("SMF", repo.settings.first()?.title)
        assertEquals(10, db.bootstrapDao().countAll())
    }

    @Test
    fun `unmappable payload fails and keeps the cached data`() = runTest {
        repo.refresh()
        val bad = TestData.bootstrapDto("BSF").let { dto ->
            dto.copy(coupons = dto.coupons.map { it.copy(type = "fixed", amount = "ten") })
        }
        api.result = AppResult.Success(bad)

        val result = assertIs<AppResult.Failure>(repo.refresh())

        assertIs<AppError.Unexpected>(result.error)
        assertEquals("SMF", repo.settings.first()?.title)
    }
}
