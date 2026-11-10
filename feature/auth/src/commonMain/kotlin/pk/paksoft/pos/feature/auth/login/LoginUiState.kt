package pk.paksoft.pos.feature.auth.login

import pk.paksoft.pos.core.ui.UiText

/** [shopId] keys the shop's identity and shared elements; [shopHost] is its address (`pbsf.org.pk/smf`). */
data class LoginUiState(
    val shopId: String = "",
    val shopName: String = "",
    val shopHost: String = "",
    val isDemoShop: Boolean = false,
    val login: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val loginError: UiText? = null,
    val passwordError: UiText? = null,
    val banner: UiText? = null,
) {
    /**
     * Whether the header's back arrow and system Back may return to the shop picker: not while a login runs or has
     * just succeeded (the button stays loading while home takes over), since switching then would end the session
     * being created.
     */
    val canChangeShop: Boolean get() = !isSubmitting
}

sealed interface LoginAction {
    data class LoginChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object Submit : LoginAction
    data object ChangeShop : LoginAction
}
