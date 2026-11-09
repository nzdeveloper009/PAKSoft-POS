package pk.paksoft.pos.core.data

import kotlinx.coroutines.flow.StateFlow
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.UserSession

/** The app's session state machine: which shop is active and whether a user is signed in to it. */
interface SessionRepository {
    val state: StateFlow<SessionState>

    /** Every shop this build can connect to. */
    val shops: List<Shop>

    /** Restores the saved shop and session; call once when the app starts. */
    suspend fun start()

    suspend fun selectShop(shopId: String)

    suspend fun login(login: String, password: String): AppResult<UserSession>

    /** Re-reads the user and permissions from the server (`GET /me`). */
    suspend fun refreshSession(): AppResult<UserSession>

    suspend fun logout()

    /** Logs out of the current shop and returns to the shop picker. */
    suspend fun switchShop()
}
