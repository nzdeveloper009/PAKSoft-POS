package pk.paksoft.pos.core.network.api

import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.dto.LoginRequestDto
import pk.paksoft.pos.core.network.dto.SessionDto

interface AuthApi {
    suspend fun login(request: LoginRequestDto): AppResult<SessionDto>
    suspend fun logout(): AppResult<Unit>
    suspend fun me(): AppResult<SessionDto>
}
