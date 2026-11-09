package pk.paksoft.pos.feature.auth.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShopPickerRoute(vm: ShopPickerViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ShopPickerScreen(state = state, onAction = vm::onAction)
}
