package pk.paksoft.pos.core.network.demo

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pk.paksoft.pos.core.model.Permissions
import pk.paksoft.pos.core.network.PosJson
import pk.paksoft.pos.core.network.dto.BootstrapDto
import pk.paksoft.pos.core.network.dto.DataEnvelope
import pk.paksoft.pos.core.network.dto.ErrorEnvelopeDto
import pk.paksoft.pos.core.network.dto.SessionDto

class DemoServerTest {
    private val server = DemoServer()

    @Test fun `login answers a session envelope`() {
        val answer = server.respond(HttpMethod.Post, "/api/v1/auth/login")
        assertEquals(HttpStatusCode.OK, answer.status)
        val session = PosJson.decodeFromString(DataEnvelope.serializer(SessionDto.serializer()), answer.body.orEmpty()).data
        assertEquals("demo-token", session.token)
        assertTrue(Permissions.SALES_ADD in session.permissions)
        assertTrue(Permissions.SALES_INDEX in session.permissions)
    }

    @Test fun `me and bootstrap decode`() {
        val me = server.respond(HttpMethod.Get, "/api/v1/me")
        assertEquals(HttpStatusCode.OK, me.status)
        PosJson.decodeFromString(DataEnvelope.serializer(SessionDto.serializer()), me.body.orEmpty())
        val boot = server.respond(HttpMethod.Get, "/api/v1/bootstrap")
        assertEquals(HttpStatusCode.OK, boot.status)
        val dto = PosJson.decodeFromString(DataEnvelope.serializer(BootstrapDto.serializer()), boot.body.orEmpty()).data
        assertTrue(dto.shop.features.ledger)
    }

    @Test fun `logout answers 204 with no body`() {
        val answer = server.respond(HttpMethod.Post, "/api/v1/auth/logout")
        assertEquals(HttpStatusCode.NoContent, answer.status)
        assertNull(answer.body)
    }

    @Test fun `unknown path answers the not_found envelope`() {
        val answer = server.respond(HttpMethod.Get, "/api/v1/nope")
        assertEquals(HttpStatusCode.NotFound, answer.status)
        assertEquals("not_found", PosJson.decodeFromString(ErrorEnvelopeDto.serializer(), answer.body.orEmpty()).error.code)
    }
}
