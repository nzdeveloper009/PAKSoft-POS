package pk.paksoft.pos.core.data

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.common.ApplicationScope
import pk.paksoft.pos.core.common.map
import pk.paksoft.pos.core.data.mapper.toUserSession
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.datastore.TokenStore
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.model.SessionNotice
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.UserSession
import pk.paksoft.pos.core.network.NoActiveShopException
import pk.paksoft.pos.core.network.PosJson
import pk.paksoft.pos.core.network.SessionEvents
import pk.paksoft.pos.core.network.api.AuthApi
import pk.paksoft.pos.core.network.dto.LoginRequestDto
import pk.paksoft.pos.core.network.dto.SessionDto

/**
 * Every state change runs under [mutex], so login, logout, switch and a 401 expiry never interleave.
 * `GET /me` is the exception: it runs outside the lock so a slow network can't hold up a logout, and
 * its answer is dropped if any other transition happened while it was in flight.
 *
 * The token lives only in [TokenStore]; the cached session JSON is the `SessionDto` without it.
 */
@Singleton
class DefaultSessionRepository(
    private val prefs: AppPreferences,
    private val tokens: TokenStore,
    private val catalog: ShopCatalog,
    private val authApi: AuthApi,
    private val cleaner: LocalDataCleaner,
    events: SessionEvents,
    scope: ApplicationScope,
    @Provided config: AppConfig,
) : SessionRepository {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Starting)
    override val state: StateFlow<SessionState> = mutableState.asStateFlow()
    override val shops: List<Shop> get() = catalog.shops

    /** Bumped on every transition (guarded by [mutex]); a `/me` answer from an older generation is stale. */
    private var generation = 0L

    private val deviceName = "${config.platformName} (PAKSOFT POS ${config.appVersion})"

    init {
        // UNDISPATCHED subscribes before the constructor returns, so no 401 can be missed.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            events.unauthenticated.collect { expire() }
        }
    }

    /** A saved session shows at once (offline too); `/me` then refreshes it, and only a 401 ends it. */
    override suspend fun start() {
        val shop = mutex.withLock {
            val shop = activeShop()
            if (shop == null) return transition(SessionState.NoShop)
            if (tokens.read(shop.id) == null) return transition(SessionState.LoggedOut(shop, null))
            transition(readCachedSession()?.let { SessionState.LoggedIn(shop, it) } ?: SessionState.Starting)
            shop
        }
        if (refreshSession() is AppResult.Failure) {
            mutex.withLock {
                // Without a cached session there is nothing to show, so the user logs in again.
                if (mutableState.value == SessionState.Starting) transition(SessionState.LoggedOut(shop, null))
            }
        }
    }

    override suspend fun selectShop(shopId: String) = mutex.withLock {
        val shop = requireNotNull(catalog.byId(shopId)) { "Unknown shop \"$shopId\"" }
        prefs.setActiveShopId(shop.id)
        transition(SessionState.LoggedOut(shop, null))
    }

    override suspend fun login(login: String, password: String): AppResult<UserSession> = mutex.withLock {
        val shop = activeShop() ?: return@withLock AppResult.Failure(AppError.Unexpected(NoActiveShopException()))
        val dto = when (val result = authApi.login(LoginRequestDto(login.trim(), password, deviceName))) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return@withLock result
        }
        val token = dto.token
            ?: return@withLock AppResult.Failure(AppError.Unexpected(IllegalStateException("Login response has no token")))
        tokens.write(shop.id, token)
        val session = saveSession(dto)
        transition(SessionState.LoggedIn(shop, session))
        AppResult.Success(session)
    }

    override suspend fun refreshSession(): AppResult<UserSession> {
        val (shop, startedAt) = mutex.withLock {
            val shop = activeShop()
            if (shop == null || tokens.read(shop.id) == null) return AppResult.Failure(AppError.Unauthenticated)
            shop to generation
        }
        val result = authApi.me()
        return mutex.withLock {
            // Logout, switch or expiry happened meanwhile: report the answer but don't apply it.
            if (startedAt != generation) return@withLock result.map { it.toUserSession() }
            when (result) {
                is AppResult.Success -> {
                    val session = saveSession(result.value)
                    transition(SessionState.LoggedIn(shop, session))
                    AppResult.Success(session)
                }
                is AppResult.Failure -> {
                    if (result.error == AppError.Unauthenticated) expireLocked(shop)
                    result
                }
            }
        }
    }

    override suspend fun logout() = mutex.withLock {
        val shop = activeShop()
        if (shop == null) {
            transition(SessionState.NoShop)
        } else {
            endSession(shop)
            transition(SessionState.LoggedOut(shop, null))
        }
    }

    /** Also clears local data when not logged in, so the next shop never sees this one's cache. */
    override suspend fun switchShop() = mutex.withLock {
        activeShop()?.let { endSession(it) }
        prefs.setActiveShopId(null)
        transition(SessionState.NoShop)
    }

    /** Any 401 ends a signed-in session (docs/API.md 1.3). */
    private suspend fun expire() = mutex.withLock {
        val current = mutableState.value
        if (current is SessionState.LoggedIn) expireLocked(current.shop)
    }

    private suspend fun expireLocked(shop: Shop) {
        clearLocalSession(shop)
        transition(SessionState.LoggedOut(shop, SessionNotice.SessionExpired))
    }

    /** Revokes the token on the server when there is one (the result is ignored: the device forgets it anyway). */
    private suspend fun endSession(shop: Shop) {
        if (tokens.read(shop.id) != null) authApi.logout()
        clearLocalSession(shop)
    }

    private suspend fun clearLocalSession(shop: Shop) {
        tokens.clear(shop.id)
        prefs.setCachedSessionJson(null)
        cleaner.clearAll()
    }

    private suspend fun saveSession(dto: SessionDto): UserSession {
        prefs.setCachedSessionJson(PosJson.encodeToString(SessionDto.serializer(), dto.copy(token = null)))
        return dto.toUserSession()
    }

    /** An unreadable cache counts as no cache. */
    private suspend fun readCachedSession(): UserSession? {
        val json = prefs.cachedSessionJson.first() ?: return null
        return try {
            PosJson.decodeFromString(SessionDto.serializer(), json).toUserSession()
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private suspend fun activeShop(): Shop? = catalog.byId(prefs.activeShopId.first())

    private fun transition(next: SessionState) {
        generation++
        mutableState.value = next
    }
}
