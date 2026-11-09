package pk.paksoft.pos.core.network

/** Supplies the API base URL of the shop the user is working in. Implemented in `core:data`. */
interface ActiveShopProvider {
    /** The active shop's base URL such as `https://host/smf/api/v1`, or null when no shop is chosen. */
    suspend fun currentApiBaseUrl(): String?
}
