package pk.paksoft.pos.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.ApiCaller
import pk.paksoft.pos.core.network.ApiEndpoint
import pk.paksoft.pos.core.network.SkipAuth
import pk.paksoft.pos.core.network.dto.LoginRequestDto
import pk.paksoft.pos.core.network.dto.SessionDto

@Singleton
class KtorAuthApi(
    private val client: HttpClient,
    private val caller: ApiCaller,
    private val endpoint: ApiEndpoint,
) : AuthApi {
    override suspend fun login(request: LoginRequestDto): AppResult<SessionDto> =
        caller.call(SessionDto.serializer()) {
            client.post(endpoint.url("auth/login")) {
                attributes.put(SkipAuth, true)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun logout(): AppResult<Unit> =
        caller.callUnit { client.post(endpoint.url("auth/logout")) }

    override suspend fun me(): AppResult<SessionDto> =
        caller.call(SessionDto.serializer()) { client.get(endpoint.url("me")) }
}
