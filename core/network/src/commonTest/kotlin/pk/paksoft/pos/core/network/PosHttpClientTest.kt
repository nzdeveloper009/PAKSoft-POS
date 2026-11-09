package pk.paksoft.pos.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.model.AppConfig

private class FakeTokens(var values: ArrayDeque<String?>) : AuthTokenProvider {
    override suspend fun token(): String? = values.removeFirstOrNull()
}

private class FakePrefs(private val id: String) : AppPreferences {
    override val activeShopId: Flow<String?> = emptyFlow()
    override suspend fun setActiveShopId(id: String?) = Unit
    override suspend fun deviceId(): String = id
    override val cachedSessionJson: Flow<String?> = emptyFlow()
    override suspend fun setCachedSessionJson(json: String?) = Unit
}

class PosHttpClientTest {
    private val seen = mutableListOf<HttpRequestData>()
    private val config = AppConfig(false, "Android", "16", "0.1.0", "http://l")

    private fun client(vararg tokens: String?) = createPosHttpClient(
        MockEngine { request ->
            seen += request
            respond("{}", HttpStatusCode.OK)
        },
        PosJson,
        FakeTokens(ArrayDeque(tokens.toList())),
        FakePrefs("dev-1"),
        config,
    )

    @Test
    fun `adds standard headers`() = runTest {
        client("t1").get("http://x/me")
        val h = seen.single().headers
        assertEquals("application/json", h[HttpHeaders.Accept])
        assertEquals("en", h[HttpHeaders.AcceptLanguage])
        assertEquals("PaksoftPOS/0.1.0 (Android 16)", h[HttpHeaders.UserAgent])
        assertEquals("dev-1", h["X-Device-Id"])
    }

    @Test
    fun `adds bearer token when present`() = runTest {
        client("t1").get("http://x/me")
        assertEquals("Bearer t1", seen.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `no header when there is no token`() = runTest {
        client(null).get("http://x/me")
        assertNull(seen.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `skips auth header for login`() = runTest {
        client("t1").get("http://x/auth/login") { attributes.put(SkipAuth, true) }
        assertNull(seen.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `token is read per request for the active shop`() = runTest {
        val c = client("a", "b")
        c.get("http://x/me")
        c.get("http://x/me")
        assertEquals(listOf("Bearer a", "Bearer b"), seen.map { it.headers[HttpHeaders.Authorization] })
    }

    @Test
    fun `endpoint joins base url and path`() = runTest {
        val withShop = ApiEndpoint(object : ActiveShopProvider {
            override suspend fun currentApiBaseUrl() = "https://x/api/v1/"
        })
        assertEquals("https://x/api/v1/me", withShop.url("/me"))
        val noShop = ApiEndpoint(object : ActiveShopProvider {
            override suspend fun currentApiBaseUrl(): String? = null
        })
        assertFailsWith<NoActiveShopException> { noShop.url("me") }
    }
}
