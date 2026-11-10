package pk.paksoft.pos.app

import androidx.compose.animation.ContentTransform
import androidx.compose.ui.unit.Density
import pk.paksoft.pos.core.designsystem.motion.PosTransitions
import pk.paksoft.pos.core.designsystem.theme.PosMotion
import pk.paksoft.pos.core.model.SessionState

/** The screens of the session gate, declared in the order the user moves through them. */
enum class SessionScreen { Starting, ShopPicker, Login, Home }

/** How the session gate moves from one screen to the next. */
enum class SessionTransition { SharedForward, SharedBack, FadeThrough }

fun sessionScreen(state: SessionState): SessionScreen = when (state) {
    SessionState.Starting -> SessionScreen.Starting
    SessionState.NoShop -> SessionScreen.ShopPicker
    is SessionState.LoggedOut -> SessionScreen.Login
    is SessionState.LoggedIn -> SessionScreen.Home
}

/**
 * The gate's `AnimatedContent` content key: a new screen, or the login of another shop, animates; a refreshed session
 * or a notice does not. A plain String on purpose: Compose keys saved state by the key's hashCode, and a String's is
 * the same in every process, while an enum's (also inside a Pair) changes, losing saved state after process death.
 */
fun sessionContentKey(state: SessionState): String {
    val screen = sessionScreen(state).name
    return if (state is SessionState.LoggedOut) "$screen:${state.shop.id}" else screen
}

/**
 * Leaving the start-up screen and arriving home fade through (no direction to show); otherwise moving deeper
 * (picker → login) is shared axis forward and moving shallower (log out, change shop) is shared axis back.
 */
fun sessionTransition(from: SessionScreen, to: SessionScreen): SessionTransition = when {
    from == SessionScreen.Starting || to == SessionScreen.Home -> SessionTransition.FadeThrough
    to.ordinal > from.ordinal -> SessionTransition.SharedForward
    to.ordinal < from.ordinal -> SessionTransition.SharedBack
    else -> SessionTransition.FadeThrough
}

internal fun SessionTransition.contentTransform(motion: PosMotion, density: Density): ContentTransform = when (this) {
    SessionTransition.SharedForward -> PosTransitions.sharedAxisForward(motion, density)
    SessionTransition.SharedBack -> PosTransitions.sharedAxisBack(motion, density)
    SessionTransition.FadeThrough -> PosTransitions.fadeThrough(motion)
}
