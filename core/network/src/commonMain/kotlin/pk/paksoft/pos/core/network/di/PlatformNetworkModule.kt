package pk.paksoft.pos.core.network.di

import org.koin.core.annotation.Module

/** Provides the platform `HttpClientEngine`: OkHttp on Android, Darwin on iOS. */
@Module
expect class PlatformNetworkModule
