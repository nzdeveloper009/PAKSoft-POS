package pk.paksoft.pos.core.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.SessionRepository
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.model.UserSession

/** Records calls and returns canned results; it never changes [state] by itself, tests set it. */
class FakeSessionRepository(
    initial: SessionState = SessionState.NoShop,
    override val shops: List<Shop> = Shops.forBuild(true, "http://local"),
) : SessionRepository {
    override val state: MutableStateFlow<SessionState> = MutableStateFlow(initial)

    var loginResult: AppResult<UserSession> = AppResult.Success(TestData.userSession())

    /** When set, [login] records the call and then suspends until the gate completes. */
    var loginGate: CompletableDeferred<Unit>? = null

    private val recordedShopIds = mutableListOf<String>()
    val selectedShopIds: List<String> get() = recordedShopIds.toList()

    private val recordedLogins = mutableListOf<Pair<String, String>>()
    val loginCalls: List<Pair<String, String>> get() = recordedLogins.toList()

    var logoutCalls: Int = 0
        private set
    var switchShopCalls: Int = 0
        private set
    var startCalls: Int = 0
        private set

    override suspend fun start() {
        startCalls++
    }

    override suspend fun selectShop(shopId: String) {
        recordedShopIds += shopId
    }

    override suspend fun login(login: String, password: String): AppResult<UserSession> {
        recordedLogins += login to password
        loginGate?.await()
        return loginResult
    }

    /** The current session when logged in, else `Unauthenticated`. */
    override suspend fun refreshSession(): AppResult<UserSession> =
        (state.value as? SessionState.LoggedIn)?.let { AppResult.Success(it.session) }
            ?: AppResult.Failure(AppError.Unauthenticated)

    override suspend fun logout() {
        logoutCalls++
    }

    override suspend fun switchShop() {
        switchShopCalls++
    }
}
