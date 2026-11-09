package pk.paksoft.pos.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.BootstrapRepository
import pk.paksoft.pos.core.data.SessionRepository
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.ShopSettings
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.toUiText

data class MainUiState(
    val settings: ShopSettings? = null,
    val banner: UiText? = null,
)

sealed interface MainAction {
    data object RetryRefresh : MainAction
}

/**
 * Lives at the root, so it outlives logins: it refreshes the shop data every time the session becomes
 * `LoggedIn` (app start with a saved session, or a new login) and cancels that refresh when it ends.
 */
@KoinViewModel
class MainViewModel(
    private val bootstrap: BootstrapRepository,
    session: SessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            bootstrap.settings.collect { settings -> _state.update { it.copy(settings = settings) } }
        }
        viewModelScope.launch {
            session.state
                .map { it is SessionState.LoggedIn }
                .distinctUntilChanged()
                .collect { loggedIn -> if (loggedIn) refresh() else refreshJob?.cancel() }
        }
    }

    fun onAction(action: MainAction) {
        when (action) {
            MainAction.RetryRefresh -> refresh()
        }
    }

    private fun refresh() {
        refreshJob?.cancel()
        _state.update { it.copy(banner = null) }
        refreshJob = viewModelScope.launch {
            val result = bootstrap.refresh()
            // Unauthenticated already ends the session and returns to login with its own notice.
            if (result is AppResult.Failure && result.error != AppError.Unauthenticated) {
                _state.update { it.copy(banner = result.error.toUiText()) }
            }
        }
    }
}
