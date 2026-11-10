package pk.paksoft.pos.core.network.demo

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

internal data class DemoResponse(val status: HttpStatusCode, val body: String?)

/** Answers the few endpoints the app needs with fixed demo data; every other path is `not_found`. */
internal class DemoServer {
    fun respond(method: HttpMethod, encodedPath: String): DemoResponse {
        val path = encodedPath.substringAfter("/api/v1/", missingDelimiterValue = "").trimEnd('/')
        return when {
            method == HttpMethod.Post && path == "auth/login" -> DemoResponse(HttpStatusCode.OK, DemoFixtures.LOGIN)
            method == HttpMethod.Post && path == "auth/logout" -> DemoResponse(HttpStatusCode.NoContent, null)
            method == HttpMethod.Get && path == "me" -> DemoResponse(HttpStatusCode.OK, DemoFixtures.ME)
            method == HttpMethod.Get && path == "bootstrap" -> DemoResponse(HttpStatusCode.OK, DemoFixtures.BOOTSTRAP)
            else -> DemoResponse(HttpStatusCode.NotFound, DemoFixtures.NOT_FOUND)
        }
    }
}
