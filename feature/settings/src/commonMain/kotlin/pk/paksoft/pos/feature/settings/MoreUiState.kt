package pk.paksoft.pos.feature.settings

import pk.paksoft.pos.core.ui.UiText

/** [shopId] picks the shop's identity; [shopHost] is its address (`pbsf.org.pk/smf`). */
data class MoreUiState(
    val userName: String = "",
    val roleName: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val shopHost: String = "",
    val isRefreshing: Boolean = false,
    val message: UiText? = null,
    val isMessageError: Boolean = false,
)

sealed interface MoreAction {
    data object RefreshData : MoreAction
    data object SwitchShop : MoreAction
    data object Logout : MoreAction
}
