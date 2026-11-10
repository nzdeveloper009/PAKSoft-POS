package pk.paksoft.pos.core.designsystem.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * Rounds a screen's corners up to [PosTransitions.PredictiveBackCorner], and lifts it with a light shadow so its edge
 * shows, while a predictive back gesture pulls it away. Both follow the screen's exit transition, which the gesture
 * seeks: they grow with the swipe, keep going while a completed gesture finishes and unwind when it is cancelled.
 * Ordinary pushes, pops and tab switches leave the screen square and flat.
 *
 * Put it on the screen's outermost layout. [visibilityScope] is the scope the screen enters and leaves with;
 * [inGesture] is true while a predictive back gesture runs on the display showing it. Does nothing without a scope
 * or with reduced motion.
 */
@Composable
fun Modifier.predictiveBackCorners(visibilityScope: AnimatedVisibilityScope?, inGesture: Boolean): Modifier {
    val motion = PosTheme.motion
    if (visibilityScope == null || motion.reduced) return this
    val transition = visibilityScope.transition
    val leaving = transition.targetState == EnterExitState.PostExit
    var pulled by remember { mutableStateOf(false) }
    LaunchedEffect(inGesture, leaving) { pulled = predictiveBackPulled(pulled, inGesture, leaving) }
    // The same linear gesture timeline as the shrink (PosTransitions.predictiveBack), so both follow the finger.
    val pull by transition.animateFloat(
        transitionSpec = { motion.gesture() },
        label = "predictiveBackPull",
    ) { state -> if (state == EnterExitState.PostExit && pulled) 1f else 0f }
    return graphicsLayer {
        val fraction = pull
        clip = fraction > 0f
        if (clip) {
            shape = RoundedCornerShape(PosTransitions.PredictiveBackCorner * fraction)
            shadowElevation = PosTransitions.PredictiveBackElevation.toPx() * fraction
        } else {
            shadowElevation = 0f
        }
    }
}

/**
 * Whether the screen is being pulled away by a predictive back gesture: set once a gesture runs while the screen
 * is leaving, kept after the gesture completes (the screen is still finishing its exit), and cleared as soon as the
 * screen stays (a cancelled gesture, or the screen the gesture reveals).
 */
internal fun predictiveBackPulled(pulled: Boolean, inGesture: Boolean, leaving: Boolean): Boolean = when {
    !leaving -> false
    inGesture -> true
    else -> pulled
}
