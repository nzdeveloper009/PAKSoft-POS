package pk.paksoft.pos.core.network.demo

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.network.ActiveShopProvider
import pk.paksoft.pos.core.network.ApiCaller
import pk.paksoft.pos.core.network.ApiEndpoint
import pk.paksoft.pos.core.network.AuthTokenProvider
import pk.paksoft.pos.core.network.PosJson
import pk.paksoft.pos.core.network.SessionEvents
import pk.paksoft.pos.core.network.api.KtorAuthApi
import pk.paksoft.pos.core.network.createPosHttpClient
import pk.paksoft.pos.core.network.dto.LoginRequestDto

private class Tokens : AuthTokenProvider {
    override suspend fun token(): String? = "t1"
}

private class Prefs : AppPreferences {
    override val activeShopId: Flow<String?> = emptyFlow()
    override suspend fun setActiveShopId(id: String?) = Unit
    override suspend fun deviceId(): String = "dev-1"
    override val cachedSessionJson: Flow<String?> = emptyFlow()
    override suspend fun setCachedSessionJson(json: String?) = Unit
}

class DemoAwareEngineTest {
    private val seen = mutableListOf<HttpRequestData>()

    private fun client(isDebug: Boolean) = createPosHttpClient(
        MockEngine { request ->
            seen += request
            respond("{}", HttpStatusCode.OK)
        },
        PosJson,
        Tokens(),
        Prefs(),
        AppConfig(isDebug, "Android", "16", "0.1.0", "http://l"),
    )

    @Test fun `demo host is answered in-app and never reaches the real engine`() = runTest {
        val response = client(true).get("https://${Shops.DEMO_HOST}/api/v1/me")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, seen.size)
    }

    @Test fun `other hosts pass through unchanged`() = runTest {
        client(true).get("https://example.test/api/v1/me")
        val request = seen.single()
        assertEquals("https://example.test/api/v1/me", request.url.toString())
        assertEquals("Bearer t1", request.headers[HttpHeaders.Authorization])
        assertEquals("dev-1", request.headers["X-Device-Id"])
    }

    @Test fun `release config does not wrap the engine`() = runTest {
        client(false).get("https://${Shops.DEMO_HOST}/api/v1/me")
        assertEquals(1, seen.size)
    }

    @Test fun `KtorAuthApi logs in against the demo shop end to end`() = runTest {
        val endpoint = ApiEndpoint(object : ActiveShopProvider {
            override suspend fun currentApiBaseUrl() = Shops.demo.apiBaseUrl
        })
        val api = KtorAuthApi(client(true), ApiCaller(PosJson, SessionEvents()), endpoint)
        val result = api.login(LoginRequestDto("anyone", "anything", "Test device"))
        val success = assertIs<AppResult.Success<*>>(result)
        assertNotNull(success.value)
        assertEquals("demo-token", (result as AppResult.Success).value.token)
    }
}
