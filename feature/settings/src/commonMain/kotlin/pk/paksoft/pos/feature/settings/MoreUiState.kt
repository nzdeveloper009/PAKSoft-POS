package pk.paksoft.pos.feature.settings

import pk.paksoft.pos.core.ui.UiText

data class MoreUiState(
    val userName: String = "",
    val roleName: String = "",
    val shopName: String = "",
    val isRefreshing: Boolean = false,
    val message: UiText? = null,
)

sealed interface MoreAction {
    data object RefreshData : MoreAction
    data object SwitchShop : MoreAction
    data object Logout : MoreAction
}
