package pk.paksoft.pos.feature.auth.shop

/** A shop card: [host] is the shop's address without scheme or API path (`pbsf.org.pk/smf`). */
data class ShopItem(val id: String, val name: String, val host: String, val isDevelopment: Boolean)

data class ShopPickerUiState(val shops: List<ShopItem> = emptyList())

sealed interface ShopPickerAction {
    data class ShopClicked(val id: String) : ShopPickerAction
}
