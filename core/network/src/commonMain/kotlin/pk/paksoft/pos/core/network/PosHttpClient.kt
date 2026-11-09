package pk.paksoft.pos.core.network

import co.touchlab.kermit.Logger as Kermit
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.model.AppConfig

/** Mark a request (login) that must not carry an `Authorization` header. */
val SkipAuth: AttributeKey<Boolean> = AttributeKey("SkipAuth")

internal class PosHeadersConfig {
    lateinit var tokens: AuthTokenProvider
    lateinit var prefs: AppPreferences
    lateinit var config: AppConfig
}

/** Sets the standard headers and the bearer token, both read fresh on every request (no caching). */
internal val PosHeaders = createClientPlugin("PosHeaders", ::PosHeadersConfig) {
    val tokens = pluginConfig.tokens
    val prefs = pluginConfig.prefs
    val config = pluginConfig.config
    val userAgent = "PaksoftPOS/${config.appVersion} (${config.platformName} ${config.osVersion})"
    onRequest { request, _ ->
        request.headers[HttpHeaders.Accept] = "application/json"
        request.headers[HttpHeaders.AcceptLanguage] = "en"
        request.headers[HttpHeaders.UserAgent] = userAgent
        request.headers["X-Device-Id"] = prefs.deviceId()
        if (!request.attributes.contains(SkipAuth)) {
            tokens.token()?.let { request.headers[HttpHeaders.Authorization] = "Bearer $it" }
        }
    }
}

fun createPosHttpClient(
    engine: HttpClientEngine,
    json: Json,
    tokens: AuthTokenProvider,
    prefs: AppPreferences,
    config: AppConfig,
): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    install(PosHeaders) {
        this.tokens = tokens
        this.prefs = prefs
        this.config = config
    }
    if (config.isDebug) {
        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { it == HttpHeaders.Authorization }
            logger = object : Logger {
                override fun log(message: String) = Kermit.withTag("Http").i(message)
            }
        }
    }
}
