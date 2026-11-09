package pk.paksoft.pos.feature.auth.shop

data class ShopItem(val id: String, val name: String, val isDevelopment: Boolean)

data class ShopPickerUiState(val shops: List<ShopItem> = emptyList())

sealed interface ShopPickerAction {
    data class ShopClicked(val id: String) : ShopPickerAction
}
