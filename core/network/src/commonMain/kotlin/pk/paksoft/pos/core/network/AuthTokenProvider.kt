package pk.paksoft.pos.core.network

/** Supplies the bearer token of the active shop. Implemented in `core:data`; read on every request. */
interface AuthTokenProvider {
    suspend fun token(): String?
}
