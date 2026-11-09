package pk.paksoft.pos.core.network.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class PlatformNetworkModule {
    @Singleton
    fun engine(): HttpClientEngine = OkHttp.create()
}
