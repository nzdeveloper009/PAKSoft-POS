package pk.paksoft.pos.core.network.demo

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import pk.paksoft.pos.core.model.Shops

/** Debug builds only: answers requests for [Shops.DEMO_HOST] from [DemoServer] and hands every other request to [real]. */
@OptIn(InternalAPI::class)
internal class DemoAwareEngine(
    private val real: HttpClientEngine,
    private val server: DemoServer,
) : HttpClientEngineBase("pos-demo-aware") {
    override val config: HttpClientEngineConfig get() = real.config
    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> get() = real.supportedCapabilities

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        if (data.url.host != Shops.DEMO_HOST) return real.execute(data)
        val answer = server.respond(data.method, data.url.encodedPath)
        return HttpResponseData(
            statusCode = answer.status,
            requestTime = GMTDate(),
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(answer.body.orEmpty().encodeToByteArray()),
            callContext = callContext(),
        )
    }

    override fun close() {
        super.close()
        real.close()
    }
}
