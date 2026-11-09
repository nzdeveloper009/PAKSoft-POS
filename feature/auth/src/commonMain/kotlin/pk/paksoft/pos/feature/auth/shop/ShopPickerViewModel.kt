package pk.paksoft.pos.feature.auth.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import pk.paksoft.pos.core.data.SessionRepository

@KoinViewModel
class ShopPickerViewModel(private val session: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(
        ShopPickerUiState(session.shops.map { ShopItem(it.id, it.displayName, it.isDevelopment) }),
    )
    val state: StateFlow<ShopPickerUiState> = _state.asStateFlow()

    fun onAction(action: ShopPickerAction) {
        when (action) {
            is ShopPickerAction.ShopClicked -> viewModelScope.launch { session.selectShop(action.id) }
        }
    }
}
