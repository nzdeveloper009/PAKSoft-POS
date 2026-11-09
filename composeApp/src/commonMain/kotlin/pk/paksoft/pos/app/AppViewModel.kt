package pk.paksoft.pos.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import pk.paksoft.pos.core.data.SessionRepository
import pk.paksoft.pos.core.model.SessionState

/**
 * The session gate's state. Created once at the root (activity / iOS view controller), so it starts the
 * session exactly once; `start()` awaits `GET /me`, which is why it runs here and not in a screen's scope.
 */
@KoinViewModel
class AppViewModel(session: SessionRepository) : ViewModel() {
    val state: StateFlow<SessionState> = session.state

    init {
        viewModelScope.launch { session.start() }
    }
}
