package pk.paksoft.pos.core.network

import org.koin.core.annotation.Singleton

/** Thrown when a request is built while no shop is chosen. */
class NoActiveShopException : IllegalStateException("No active shop")

/** Resolves a relative API path against the active shop on every call. */
@Singleton
class ApiEndpoint(private val shops: ActiveShopProvider) {
    suspend fun url(path: String): String {
        val base = shops.currentApiBaseUrl() ?: throw NoActiveShopException()
        return "${base.trimEnd('/')}/${path.trimStart('/')}"
    }
}
