package pk.paksoft.pos.core.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.common.AppDispatchers
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.common.ApplicationScope
import pk.paksoft.pos.core.database.PosDatabase
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.network.SessionEvents
import pk.paksoft.pos.core.testing.FakeAuthApi
import pk.paksoft.pos.core.testing.FakeBootstrapApi
import pk.paksoft.pos.core.testing.InMemoryAppPreferences
import pk.paksoft.pos.core.testing.InMemoryTokenStore
import pk.paksoft.pos.core.testing.TestData

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultBootstrapRepositoryTest {
    private val db = Room.inMemoryDatabaseBuilder<PosDatabase>().setDriver(BundledSQLiteDriver()).build()
    private val api = FakeBootstrapApi(AppResult.Success(TestData.bootstrapDto("SMF")))
    private val epoch = SessionEpoch()
    private val repo = DefaultBootstrapRepository(api, db.bootstrapDao(), epoch)

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

    @Test
    fun `refresh answering after switch shop does not refill the cache`() = runTest {
        val d = StandardTestDispatcher(testScheduler)
        val appScope = ApplicationScope(AppDispatchers(d, d, d))
        val config = AppConfig(true, "iOS", "26", "0.1.0", "http://127.0.0.1:8010/api/v1")
        val session = DefaultSessionRepository(
            InMemoryAppPreferences(activeShopId = "smf"),
            InMemoryTokenStore(mapOf("smf" to "t")),
            ShopCatalog(config),
            FakeAuthApi(),
            RoomLocalDataCleaner(db.bootstrapDao(), epoch),
            SessionEvents(),
            appScope,
            config,
        )
        val gate = CompletableDeferred<Unit>().also { api.gate = it }

        val refresh = async { repo.refresh() }
        runCurrent()
        assertEquals(1, api.calls)

        session.switchShop()
        gate.complete(Unit)

        assertEquals(AppResult.Failure(AppError.Unexpected(null)), refresh.await())
        assertNull(repo.settings.first())
        assertEquals(0, db.bootstrapDao().countAll())
        appScope.cancel()
    }
}
