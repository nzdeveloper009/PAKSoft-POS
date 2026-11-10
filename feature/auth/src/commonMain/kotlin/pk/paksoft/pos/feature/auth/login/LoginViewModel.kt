package pk.paksoft.pos.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.SessionRepository
import pk.paksoft.pos.core.model.SessionNotice
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.shopHost
import pk.paksoft.pos.core.ui.toUiText
import pk.paksoft.pos.feature.auth.resources.Res
import pk.paksoft.pos.feature.auth.resources.login_error_login_required
import pk.paksoft.pos.feature.auth.resources.login_error_password_required
import pk.paksoft.pos.core.ui.resources.Res as UiRes
import pk.paksoft.pos.core.ui.resources.session_expired

@KoinViewModel
class LoginViewModel(private val session: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private var shopId: String? = null

    init {
        // This ViewModel outlives logins, so it resets itself every time the session returns to LoggedOut.
        viewModelScope.launch {
            session.state.collect { sessionState ->
                if (sessionState is SessionState.LoggedOut) onLoggedOut(sessionState)
            }
        }
    }

    private fun onLoggedOut(loggedOut: SessionState.LoggedOut) {
        val sameShop = shopId == loggedOut.shop.id
        shopId = loggedOut.shop.id
        _state.update {
            it.copy(
                shopId = loggedOut.shop.id,
                shopName = loggedOut.shop.displayName,
                shopHost = shopHost(loggedOut.shop.apiBaseUrl),
                isDemoShop = loggedOut.shop.id == Shops.DEMO_ID,
                login = if (sameShop) it.login else "",
                password = "",
                isSubmitting = false,
                loginError = null,
                passwordError = null,
                banner = if (loggedOut.notice == SessionNotice.SessionExpired) {
                    UiText.Res(UiRes.string.session_expired)
                } else {
                    null
                },
            )
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.LoginChanged -> _state.update { it.copy(login = action.value, loginError = null, banner = null) }
            is LoginAction.PasswordChanged ->
                _state.update { it.copy(password = action.value, passwordError = null, banner = null) }
            LoginAction.TogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginAction.Submit -> submit()
            LoginAction.ChangeShop -> changeShop()
        }
    }

    /** Ignored unless [LoginUiState.canChangeShop]: switching during a login would end the session being created. */
    private fun changeShop() {
        if (!_state.value.canChangeShop) return
        viewModelScope.launch { session.switchShop() }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        val login = current.login.trim()
        val loginError = if (login.isEmpty()) UiText.Res(Res.string.login_error_login_required) else null
        val passwordError = if (current.password.isBlank()) UiText.Res(Res.string.login_error_password_required) else null
        if (loginError != null || passwordError != null) {
            _state.update { it.copy(loginError = loginError, passwordError = passwordError, banner = null) }
            return
        }
        _state.update { it.copy(isSubmitting = true, loginError = null, passwordError = null, banner = null) }
        viewModelScope.launch {
            when (val result = session.login(login, current.password)) {
                // The session gate swaps in the home; the button stays a loading circle while this screen fades
                // out. onLoggedOut resets it when the session comes back here.
                is AppResult.Success -> Unit
                is AppResult.Failure -> _state.update { it.withFailure(result.error) }
            }
        }
    }

    private fun LoginUiState.withFailure(error: AppError): LoginUiState {
        if (error !is AppError.Validation) return copy(isSubmitting = false, banner = error.toUiText())
        val loginMessage = error.fields["login"]?.firstOrNull()
        val passwordMessage = error.fields["password"]?.firstOrNull()
        return copy(
            isSubmitting = false,
            loginError = loginMessage?.let(UiText::Raw),
            passwordError = passwordMessage?.let(UiText::Raw),
            banner = if (loginMessage == null && passwordMessage == null) UiText.Raw(error.message) else null,
        )
    }
}
