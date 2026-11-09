package pk.paksoft.pos.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Singleton

/** Raised when the server rejects the token (any HTTP 401 except `invalid_credentials`); the session must end. */
@Singleton
class SessionEvents {
    private val _unauthenticated = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val unauthenticated: SharedFlow<Unit> = _unauthenticated.asSharedFlow()

    fun emitUnauthenticated() {
        _unauthenticated.tryEmit(Unit)
    }
}
