package pk.paksoft.pos.core.network

import app.cash.turbine.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.dto.SessionDto

class ApiCallerTest {
    private val events = SessionEvents()
    private val caller = ApiCaller(PosJson, events)

    private fun clientFor(handler: suspend io.ktor.client.engine.mock.MockRequestHandleScope.() -> io.ktor.client.request.HttpResponseData) =
        HttpClient(MockEngine { handler() })

    private suspend fun respond(
        status: HttpStatusCode,
        body: String,
        contentType: ContentType = ContentType.Application.Json,
    ): AppResult<SessionDto> {
        val client = clientFor { respond(body, status, headersOf(HttpHeaders.ContentType, contentType.toString())) }
        return caller.call(SessionDto.serializer()) { client.get("http://x/me") }
    }

    private fun AppResult<*>.error(): AppError = (this as AppResult.Failure).error

    @Test
    fun `200 returns data`() = runTest {
        val r = respond(HttpStatusCode.OK, ApiFixtures.LOGIN_200)
        assertEquals(7, (r as AppResult.Success).value.user.id)
    }

    @Test
    fun `204 unit call succeeds`() = runTest {
        val client = clientFor { respond("", HttpStatusCode.NoContent) }
        assertEquals(AppResult.Success(Unit), caller.callUnit { client.get("http://x/auth/logout") })
    }

    @Test
    fun `401 unauthenticated emits session event`() = runTest {
        events.unauthenticated.test {
            val r = respond(HttpStatusCode.Unauthorized, ApiFixtures.ERROR_UNAUTHENTICATED)
            assertEquals(AppError.Unauthenticated, r.error())
            awaitItem()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `401 invalid credentials is a business error without session event`() = runTest {
        events.unauthenticated.test {
            val r = respond(HttpStatusCode.Unauthorized, ApiFixtures.ERROR_INVALID_CREDENTIALS)
            assertEquals(
                AppError.Business("invalid_credentials", "The username/email or password is incorrect.", null),
                r.error(),
            )
            expectNoEvents()
        }
    }

    @Test
    fun `account inactive is forbidden`() = runTest {
        val body = """{"error":{"code":"account_inactive","message":"Your account is inactive."}}"""
        assertEquals(AppError.Forbidden("account_inactive", "Your account is inactive."), respond(HttpStatusCode.Forbidden, body).error())
    }

    @Test
    fun `422 carries field errors`() = runTest {
        val e = respond(HttpStatusCode.UnprocessableEntity, ApiFixtures.ERROR_VALIDATION_DEVICE_NAME).error()
        assertIs<AppError.Validation>(e)
        assertEquals(listOf("The device name field is required."), e.fields["device_name"])
    }

    @Test
    fun `html 404 becomes Server 404 without message`() = runTest {
        val r = respond(HttpStatusCode.NotFound, "<html>Not Found</html>", ContentType.Text.Html)
        assertEquals(AppError.Server(404, null), r.error())
    }

    @Test
    fun `500 envelope keeps server message`() = runTest {
        val r = respond(HttpStatusCode.InternalServerError, ApiFixtures.ERROR_SERVER)
        assertEquals(AppError.Server(500, "Something went wrong on the server. Please try again."), r.error())
    }

    @Test
    fun `conflict and unknown codes keep details`() = runTest {
        val details = """{"error":{"code":"insufficient_stock","message":"No stock.","details":{"lines":[0]}}}"""
        assertEquals(AppError.Business("insufficient_stock", "No stock.", """{"lines":[0]}"""), respond(HttpStatusCode.UnprocessableEntity, details).error())
        val conflict = """{"error":{"code":"conflict","message":"Changed.","details":{"a":1}}}"""
        assertEquals(AppError.Conflict("Changed.", """{"a":1}"""), respond(HttpStatusCode.Conflict, conflict).error())
    }

    @Test
    fun `timeout maps to Timeout`() = runTest {
        val client = HttpClient(MockEngine { throw HttpRequestTimeoutException("u", 1) })
        assertEquals(AppResult.Failure(AppError.Timeout), caller.call(SessionDto.serializer()) { client.get("http://x/me") })
    }

    @Test
    fun `io failure maps to Offline`() = runTest {
        val client = HttpClient(MockEngine { throw IOException("no route") })
        assertEquals(AppResult.Failure(AppError.Offline), caller.call(SessionDto.serializer()) { client.get("http://x/me") })
    }

    @Test
    fun `malformed success body and missing shop are unexpected`() = runTest {
        assertTrue(respond(HttpStatusCode.OK, "{}").error() is AppError.Unexpected)
        val r = caller.callUnit { throw NoActiveShopException() }
        assertTrue(r.error() is AppError.Unexpected)
    }
}
