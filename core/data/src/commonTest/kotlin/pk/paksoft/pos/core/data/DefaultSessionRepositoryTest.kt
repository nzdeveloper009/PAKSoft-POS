package pk.paksoft.pos.core.data

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.common.AppDispatchers
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.common.ApplicationScope
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.model.SessionNotice
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.network.PosJson
import pk.paksoft.pos.core.network.SessionEvents
import pk.paksoft.pos.core.network.dto.SessionDto
import pk.paksoft.pos.core.testing.FakeAuthApi
import pk.paksoft.pos.core.testing.FakeLocalDataCleaner
import pk.paksoft.pos.core.testing.InMemoryAppPreferences
import pk.paksoft.pos.core.testing.InMemoryTokenStore
import pk.paksoft.pos.core.testing.TestData

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepositoryTest {
    private val config = AppConfig(true, "Android", "16", "0.1.0", "http://10.0.2.2:8010/api/v1")
    private val scopes = mutableListOf<ApplicationScope>()

    private fun shop(id: String): Shop = Shops.forBuild(true, config.localApiBaseUrl).single { it.id == id }

    private fun cachedJson(dto: SessionDto = TestData.sessionDto(token = null)): String =
        PosJson.encodeToString(SessionDto.serializer(), dto)

    private fun TestScope.repo(
        prefs: InMemoryAppPreferences = InMemoryAppPreferences(),
        tokens: InMemoryTokenStore = InMemoryTokenStore(),
        api: FakeAuthApi = FakeAuthApi(),
        cleaner: FakeLocalDataCleaner = FakeLocalDataCleaner(),
        events: SessionEvents = SessionEvents(),
    ): DefaultSessionRepository {
        val d = StandardTestDispatcher(testScheduler)
        val scope = ApplicationScope(AppDispatchers(d, d, d)).also { scopes += it }
        return DefaultSessionRepository(prefs, tokens, ShopCatalog(config), api, cleaner, events, scope, config)
    }

    @AfterTest
    fun cancelScopes() {
        scopes.forEach { it.cancel() }
    }

    @Test
    fun `start without shop is NoShop`() = runTest {
        val repo = repo()
        assertEquals(SessionState.Starting, repo.state.value)
        repo.start()
        assertEquals(SessionState.NoShop, repo.state.value)
    }

    @Test
    fun `start with an unknown shop is NoShop`() = runTest {
        val repo = repo(prefs = InMemoryAppPreferences(activeShopId = "gone"))
        repo.start()
        assertEquals(SessionState.NoShop, repo.state.value)
    }

    @Test
    fun `start with shop and no token is LoggedOut`() = runTest {
        val repo = repo(prefs = InMemoryAppPreferences(activeShopId = "smf"))
        repo.start()
        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
    }

    @Test
    fun `start offline with cached session stays logged in`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf", cachedSessionJson = cachedJson())
        val tokens = InMemoryTokenStore(mapOf("smf" to "t"))
        val api = FakeAuthApi().apply { meResult = AppResult.Failure(AppError.Offline) }
        val cleaner = FakeLocalDataCleaner()
        val repo = repo(prefs, tokens, api, cleaner)
        val loggedIn = SessionState.LoggedIn(shop("smf"), TestData.userSession())

        repo.start()
        assertEquals(loggedIn, repo.state.value)
        advanceUntilIdle()

        assertEquals(loggedIn, repo.state.value)
        assertEquals(1, api.meCalls)
        assertEquals("t", tokens.tokens["smf"])
        assertEquals(cachedJson(), prefs.cachedSessionJson.first())
        assertEquals(0, cleaner.clearCalls)
    }

    @Test
    fun `start online refreshes the cached session`() = runTest {
        val old = TestData.sessionDto(token = null, permissions = listOf("sales-index"))
        val fresh = TestData.sessionDto(token = null, permissions = listOf("sales-index", "products-index"))
        val prefs = InMemoryAppPreferences(activeShopId = "smf", cachedSessionJson = cachedJson(old))
        val api = FakeAuthApi().apply { meResult = AppResult.Success(fresh) }
        val repo = repo(prefs, InMemoryTokenStore(mapOf("smf" to "t")), api)

        repo.start()

        val session = TestData.userSession(permissions = setOf("sales-index", "products-index"))
        assertEquals(SessionState.LoggedIn(shop("smf"), session), repo.state.value)
        assertEquals(cachedJson(fresh), prefs.cachedSessionJson.first())
    }

    @Test
    fun `start with token and no cache logs in after refresh`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val repo = repo(prefs, InMemoryTokenStore(mapOf("smf" to "t")))

        repo.start()

        assertEquals(SessionState.LoggedIn(shop("smf"), TestData.userSession()), repo.state.value)
        assertEquals(cachedJson(), prefs.cachedSessionJson.first())
    }

    @Test
    fun `start with token and no cache offline is LoggedOut`() = runTest {
        val api = FakeAuthApi().apply { meResult = AppResult.Failure(AppError.Offline) }
        val repo = repo(InMemoryAppPreferences(activeShopId = "smf"), InMemoryTokenStore(mapOf("smf" to "t")), api)

        repo.start()

        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
    }

    @Test
    fun `start with a revoked token expires the session`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf", cachedSessionJson = cachedJson())
        val tokens = InMemoryTokenStore(mapOf("smf" to "t"))
        val api = FakeAuthApi().apply { meResult = AppResult.Failure(AppError.Unauthenticated) }
        val cleaner = FakeLocalDataCleaner()
        val repo = repo(prefs, tokens, api, cleaner)

        repo.start()
        advanceUntilIdle()

        assertEquals(SessionState.LoggedOut(shop("smf"), SessionNotice.SessionExpired), repo.state.value)
        assertTrue(tokens.tokens.isEmpty())
        assertNull(prefs.cachedSessionJson.first())
        assertEquals(1, cleaner.clearCalls)
    }

    @Test
    fun `logout during the start refresh is not undone by it`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf", cachedSessionJson = cachedJson())
        val tokens = InMemoryTokenStore(mapOf("smf" to "t"))
        val api = FakeAuthApi().apply { meGate = CompletableDeferred() }
        val repo = repo(prefs, tokens, api)

        launch { repo.start() }
        runCurrent()
        assertEquals(1, api.meCalls)
        assertEquals(SessionState.LoggedIn(shop("smf"), TestData.userSession()), repo.state.value)

        repo.logout()
        api.meGate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
        assertTrue(tokens.tokens.isEmpty())
        assertNull(prefs.cachedSessionJson.first())
    }

    @Test
    fun `login trims and stores token for the active shop`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "local")
        val tokens = InMemoryTokenStore()
        val api = FakeAuthApi().apply { loginResult = AppResult.Success(TestData.sessionDto(token = "tok-9")) }
        val repo = repo(prefs, tokens, api)
        repo.start()

        val result = repo.login("  cashier1  ", " pw ")

        assertEquals(AppResult.Success(TestData.userSession()), result)
        val request = api.loginCalls.single()
        assertEquals("cashier1", request.login)
        assertEquals(" pw ", request.password)
        assertEquals("Android (PAKSOFT POS 0.1.0)", request.deviceName)
        assertEquals(mapOf("local" to "tok-9"), tokens.tokens)
        assertEquals(SessionState.LoggedIn(shop("local"), TestData.userSession()), repo.state.value)
        val cache = assertNotNull(prefs.cachedSessionJson.first())
        assertFalse("tok-9" in cache)
        assertEquals(TestData.sessionDto(token = null), PosJson.decodeFromString(SessionDto.serializer(), cache))
    }

    @Test
    fun `failed login stores nothing`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val tokens = InMemoryTokenStore()
        val error = AppError.Business("invalid_credentials", "The username/email or password is incorrect.", null)
        val api = FakeAuthApi().apply { loginResult = AppResult.Failure(error) }
        val repo = repo(prefs, tokens, api)
        repo.start()

        val result = repo.login("cashier1", "wrong")

        assertEquals(AppResult.Failure(error), result)
        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
        assertTrue(tokens.tokens.isEmpty())
        assertNull(prefs.cachedSessionJson.first())
    }

    @Test
    fun `unauthenticated event expires the session`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "local")
        val tokens = InMemoryTokenStore()
        val cleaner = FakeLocalDataCleaner()
        val events = SessionEvents()
        val repo = repo(prefs, tokens, cleaner = cleaner, events = events)
        repo.start()
        repo.login("cashier1", "pw")
        assertTrue(repo.state.value is SessionState.LoggedIn)

        events.emitUnauthenticated()
        advanceUntilIdle()

        assertEquals(SessionState.LoggedOut(shop("local"), SessionNotice.SessionExpired), repo.state.value)
        assertTrue(tokens.tokens.isEmpty())
        assertEquals(1, cleaner.clearCalls)
        assertNull(prefs.cachedSessionJson.first())
    }

    @Test
    fun `unauthenticated event while logged out changes nothing`() = runTest {
        val cleaner = FakeLocalDataCleaner()
        val events = SessionEvents()
        val repo = repo(InMemoryAppPreferences(activeShopId = "smf"), cleaner = cleaner, events = events)
        repo.start()

        events.emitUnauthenticated()
        advanceUntilIdle()

        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
        assertEquals(0, cleaner.clearCalls)
    }

    @Test
    fun `logout clears even when the server call fails`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val tokens = InMemoryTokenStore()
        val api = FakeAuthApi().apply { logoutResult = AppResult.Failure(AppError.Offline) }
        val cleaner = FakeLocalDataCleaner()
        val repo = repo(prefs, tokens, api, cleaner)
        repo.start()
        repo.login("cashier1", "pw")

        repo.logout()

        assertEquals(1, api.logoutCalls)
        assertTrue(tokens.tokens.isEmpty())
        assertEquals(1, cleaner.clearCalls)
        assertNull(prefs.cachedSessionJson.first())
        assertEquals(SessionState.LoggedOut(shop("smf"), null), repo.state.value)
    }

    @Test
    fun `switch shop clears token and local data`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val tokens = InMemoryTokenStore()
        val api = FakeAuthApi()
        val cleaner = FakeLocalDataCleaner()
        val repo = repo(prefs, tokens, api, cleaner)
        repo.start()
        repo.login("cashier1", "pw")
        assertEquals("tok-1", tokens.tokens["smf"])

        repo.switchShop()

        assertNull(tokens.tokens["smf"])
        assertEquals(1, api.logoutCalls)
        assertEquals(1, cleaner.clearCalls)
        assertNull(prefs.cachedSessionJson.first())
        assertNull(prefs.activeShopId.first())
        assertEquals(SessionState.NoShop, repo.state.value)
    }

    @Test
    fun `switch shop while logged out clears local data without calling the server`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val api = FakeAuthApi()
        val cleaner = FakeLocalDataCleaner()
        val repo = repo(prefs, api = api, cleaner = cleaner)
        repo.start()

        repo.switchShop()

        assertEquals(0, api.logoutCalls)
        assertEquals(1, cleaner.clearCalls)
        assertNull(prefs.activeShopId.first())
        assertEquals(SessionState.NoShop, repo.state.value)
    }

    @Test
    fun `select shop goes to login for that shop`() = runTest {
        val prefs = InMemoryAppPreferences()
        val repo = repo(prefs)
        repo.start()

        repo.selectShop("bsf")

        assertEquals(SessionState.LoggedOut(shop("bsf"), null), repo.state.value)
        assertEquals("bsf", prefs.activeShopId.first())
    }

    @Test
    fun `shops come from the build's catalog`() = runTest {
        val repo = repo()
        assertEquals(listOf("smf", "bsf", "mummyandme", "staging", "local"), repo.shops.map { it.id })
        assertEquals(config.localApiBaseUrl, repo.shops.last().apiBaseUrl)
    }
}
