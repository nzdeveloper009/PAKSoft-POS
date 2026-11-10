package pk.paksoft.pos.feature.auth.login

import pk.paksoft.pos.core.ui.UiText

data class LoginUiState(
    val shopName: String = "",
    val isDemoShop: Boolean = false,
    val login: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val loginError: UiText? = null,
    val passwordError: UiText? = null,
    val banner: UiText? = null,
)

sealed interface LoginAction {
    data class LoginChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object Submit : LoginAction
    data object ChangeShop : LoginAction
}
