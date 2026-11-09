package pk.paksoft.pos.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoreRoute(vm: MoreViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    MoreScreen(state = state, onAction = vm::onAction)
}
