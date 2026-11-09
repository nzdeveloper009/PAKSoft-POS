package pk.paksoft.pos.core.network.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class PlatformNetworkModule {
    @Singleton
    fun engine(): HttpClientEngine = Darwin.create()
}
