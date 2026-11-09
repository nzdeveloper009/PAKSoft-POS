package pk.paksoft.pos.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.ApiCaller
import pk.paksoft.pos.core.network.ApiEndpoint
import pk.paksoft.pos.core.network.dto.BootstrapDto

@Singleton
class KtorBootstrapApi(
    private val client: HttpClient,
    private val caller: ApiCaller,
    private val endpoint: ApiEndpoint,
) : BootstrapApi {
    override suspend fun bootstrap(): AppResult<BootstrapDto> =
        caller.call(BootstrapDto.serializer()) { client.get(endpoint.url("bootstrap")) }
}
