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
import pk.paksoft.pos.core.model.ShopSettings
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.shopHost
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
            combine(session.state, bootstrap.settings, ::Pair).collect { (sessionState, settings) ->
                _state.update { it.withSession(sessionState, settings) }
            }
        }
    }

    /**
     * The signed-in user and shop (the shop's own title once bootstrap has cached it). Logging out or switching shop
     * clears the shop data and then ends the session while this screen is still animating out, so it keeps the last
     * user, shop and title instead of going blank.
     */
    private fun MoreUiState.withSession(sessionState: SessionState, settings: ShopSettings?): MoreUiState {
        val loggedIn = sessionState as? SessionState.LoggedIn ?: return this
        val shop = loggedIn.shop
        val lastTitle = shopName.takeIf { shop.id == shopId && it.isNotEmpty() }
        return copy(
            userName = loggedIn.session.user.name,
            roleName = loggedIn.session.user.roleName,
            shopId = shop.id,
            shopName = settings?.title ?: lastTitle ?: shop.displayName,
            shopDisplayName = shop.displayName,
            shopHost = shopHost(shop.apiBaseUrl),
        )
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
        _state.update { it.copy(isRefreshing = true, message = null, isMessageError = false) }
        viewModelScope.launch {
            val result = bootstrap.refresh()
            val message = when (result) {
                is AppResult.Success -> UiText.Res(Res.string.more_refreshed)
                is AppResult.Failure -> result.error.toUiText()
            }
            _state.update {
                it.copy(isRefreshing = false, message = message, isMessageError = result is AppResult.Failure)
            }
        }
    }
}
