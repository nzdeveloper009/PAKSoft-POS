package pk.paksoft.pos.core.testing

import kotlinx.coroutines.CompletableDeferred
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.api.AuthApi
import pk.paksoft.pos.core.network.dto.LoginRequestDto
import pk.paksoft.pos.core.network.dto.SessionDto

class FakeAuthApi : AuthApi {
    var loginResult: AppResult<SessionDto> = AppResult.Success(TestData.sessionDto())
    var meResult: AppResult<SessionDto> = AppResult.Success(TestData.sessionDto(token = null))
    var logoutResult: AppResult<Unit> = AppResult.Success(Unit)

    /** When set, [login] records the call and then suspends until the gate completes. */
    var loginGate: CompletableDeferred<Unit>? = null

    /** When set, [me] records the call and then suspends until the gate completes. */
    var meGate: CompletableDeferred<Unit>? = null

    private val recordedLogins = mutableListOf<LoginRequestDto>()
    val loginCalls: List<LoginRequestDto> get() = recordedLogins.toList()

    var logoutCalls: Int = 0
        private set
    var meCalls: Int = 0
        private set

    override suspend fun login(request: LoginRequestDto): AppResult<SessionDto> {
        recordedLogins += request
        loginGate?.await()
        return loginResult
    }

    override suspend fun logout(): AppResult<Unit> {
        logoutCalls++
        return logoutResult
    }

    override suspend fun me(): AppResult<SessionDto> {
        meCalls++
        meGate?.await()
        return meResult
    }
}
