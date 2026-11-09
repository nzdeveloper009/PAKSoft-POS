package pk.paksoft.pos.core.network.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.datastore.AppPreferences
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.network.AuthTokenProvider
import pk.paksoft.pos.core.network.PosJson
import pk.paksoft.pos.core.network.createPosHttpClient

@Module(includes = [PlatformNetworkModule::class])
@Configuration
@ComponentScan("pk.paksoft.pos.core.network")
class NetworkModule {
    @Singleton
    fun json(): Json = PosJson

    @Singleton
    fun httpClient(
        engine: HttpClientEngine,
        json: Json,
        tokens: AuthTokenProvider,
        prefs: AppPreferences,
        @Provided config: AppConfig,
    ): HttpClient = createPosHttpClient(engine, json, tokens, prefs, config)
}
