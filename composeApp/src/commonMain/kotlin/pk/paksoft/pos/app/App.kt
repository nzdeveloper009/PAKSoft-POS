package pk.paksoft.pos.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pk.paksoft.pos.app.main.MainScaffold
import pk.paksoft.pos.core.designsystem.component.LoadingState
import pk.paksoft.pos.core.designsystem.motion.LocalPosAnimatedVisibilityScope
import pk.paksoft.pos.core.designsystem.motion.LocalSharedTransitionScope
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.feature.auth.login.LoginRoute
import pk.paksoft.pos.feature.auth.shop.ShopPickerRoute

/**
 * The session gate: shop picker, login or the signed-in home, driven only by [SessionState]. It animates between
 * them ([sessionTransition]) inside the app's one `SharedTransitionLayout`, and gives each screen the gate's animated
 * scope so the shop picker and login can share elements.
 */
@Composable
fun App(vm: AppViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    PosTheme {
        val motion = PosTheme.motion
        val density = LocalDensity.current
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = state,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        sessionTransition(sessionScreen(initialState), sessionScreen(targetState))
                            .contentTransform(motion, density)
                    },
                    label = "sessionGate",
                    // A new screen, or the login of another shop, animates; a refreshed session or notice does not.
                    contentKey = { sessionScreen(it) to (it as? SessionState.LoggedOut)?.shop?.id },
                ) { current ->
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalPosAnimatedVisibilityScope provides this@AnimatedContent,
                    ) {
                        when (current) {
                            SessionState.Starting -> LoadingState()
                            SessionState.NoShop -> SafeArea { ShopPickerRoute() }
                            is SessionState.LoggedOut -> SafeArea { key(current.shop.id) { LoginRoute() } }
                            is SessionState.LoggedIn -> MainScaffold(current.session)
                        }
                    }
                }
            }
        }
    }
}

/** The app draws edge to edge; screens without an app bar stay clear of the system bars and the keyboard. */
@Composable
private fun SafeArea(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) { content() }
}
