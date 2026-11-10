package pk.paksoft.pos.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel

/**
 * The login of the selected shop. System Back (and the predictive back gesture) does what the header's back arrow
 * does: back to the shop picker. While a login runs ([LoginUiState.canChangeShop] is false) Back is left to the
 * system, as on the shop picker, where Back leaves the app.
 */
@Composable
fun LoginRoute(vm: LoginViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.canChangeShop,
        onBackCompleted = { vm.onAction(LoginAction.ChangeShop) },
    )
    LoginScreen(state = state, onAction = vm::onAction)
}
