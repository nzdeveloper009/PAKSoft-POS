package pk.paksoft.pos.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(vm: LoginViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LoginScreen(state = state, onAction = vm::onAction)
}
