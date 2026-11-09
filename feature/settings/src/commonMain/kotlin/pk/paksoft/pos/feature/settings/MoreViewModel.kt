package pk.paksoft.pos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.BootstrapRepository
import pk.paksoft.pos.core.data.SessionRepository
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.toUiText
import pk.paksoft.pos.feature.settings.resources.Res
import pk.paksoft.pos.feature.settings.resources.more_refreshed

@KoinViewModel
class MoreViewModel(
    private val session: SessionRepository,
    private val bootstrap: BootstrapRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MoreUiState())
    val state: StateFlow<MoreUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(session.state, bootstrap.settings) { sessionState, settings ->
                val loggedIn = sessionState as? SessionState.LoggedIn
                Triple(loggedIn?.session?.user?.name.orEmpty(), loggedIn?.session?.user?.roleName.orEmpty(), settings?.title ?: loggedIn?.shop?.displayName.orEmpty())
            }.collect { (userName, roleName, shopName) ->
                _state.update { it.copy(userName = userName, roleName = roleName, shopName = shopName) }
            }
        }
    }

    fun onAction(action: MoreAction) {
        when (action) {
            MoreAction.RefreshData -> refresh()
            MoreAction.SwitchShop -> viewModelScope.launch { session.switchShop() }
            MoreAction.Logout -> viewModelScope.launch { session.logout() }
        }
    }

    private fun refresh() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true, message = null) }
        viewModelScope.launch {
            val message = when (val result = bootstrap.refresh()) {
                is AppResult.Success -> UiText.Res(Res.string.more_refreshed)
                is AppResult.Failure -> result.error.toUiText()
            }
            _state.update { it.copy(isRefreshing = false, message = message) }
        }
    }
}
