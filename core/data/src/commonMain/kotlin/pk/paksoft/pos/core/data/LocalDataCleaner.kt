package pk.paksoft.pos.core.data

/** Deletes every cached row of the current shop (one database, one shop at a time). */
interface LocalDataCleaner {
    suspend fun clearAll()
}
