package pk.paksoft.pos.core.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Singleton

/**
 * Orders writes to the local cache against the end of a session. Clearing local data starts a new epoch;
 * a refresh notes the epoch before its network call and writes only if no clear happened since, so an
 * answer for the previous shop or user can never refill the cache after logout or switch shop.
 *
 * Lock order: the session repository's mutex may be held when [clear] is called, never the other way
 * round. Blocks passed here must only touch the database, never the session repository.
 */
@Singleton
class SessionEpoch {
    private val mutex = Mutex()
    private var epoch = 0L

    suspend fun current(): Long = mutex.withLock { epoch }

    /** Starts a new epoch and runs [block] (the clear) under the lock; the epoch moves on even if [block] throws. */
    suspend fun clear(block: suspend () -> Unit) = mutex.withLock {
        epoch++
        block()
    }

    /** Runs [block] under the lock only if the epoch is still [startedAt]; returns whether it ran. */
    suspend fun writeIfCurrent(startedAt: Long, block: suspend () -> Unit): Boolean = mutex.withLock {
        if (epoch != startedAt) return@withLock false
        block()
        true
    }
}
