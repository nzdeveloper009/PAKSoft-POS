package pk.paksoft.pos.core.designsystem.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.theme.PosMotion

/**
 * Screen-to-screen transitions, built from [PosMotion] so reduced motion applies (slides and scales snap, fades
 * become the short tween). The session gate and `NavDisplay` both use them; none sets a size transform.
 *
 * - Shared axis X (push / pop, log in to a shop, log out): the incoming screen slides 40 dp and fades in with
 *   `spatial`; the outgoing one slides 40 dp the same way and fades out with `fastEffects`.
 * - Fade through (login → home, tab switches): the outgoing screen fades out with `fastEffects`; the incoming one
 *   fades in and scales 0.94 → 1 with `spatial`.
 * - Predictive back: the previous screen comes in as on the way back while the leaving one shrinks to 0.9 and
 *   rounds its corners ([predictiveBackCorners]), in step with the swipe.
 *
 * The slide needs [Density] to turn 40 dp into pixels, since a [ContentTransform] is built outside composition.
 */
object PosTransitions {
    /** How far a screen slides on the shared X axis. */
    val SharedAxisDistance: Dp = 40.dp

    /** The incoming screen's starting scale in a fade through. */
    const val FadeThroughInitialScale: Float = 0.94f

    /** The leaving screen's scale at the end of a predictive back gesture. */
    const val PredictiveBackScale: Float = 0.9f

    /** The leaving screen's corner radius at the end of a predictive back gesture (`extraLarge`). */
    val PredictiveBackCorner: Dp = 28.dp

    /** The leaving screen's shadow at the end of a predictive back gesture, so its edge shows over the screen below. */
    val PredictiveBackElevation: Dp = 6.dp

    private const val PREDICTIVE_BACK_OPAQUE_SHARE = 0.7f

    /** Forward on the X axis: the new screen comes in from the end side, the old one leaves toward the start. */
    fun sharedAxisForward(motion: PosMotion, density: Density): ContentTransform {
        val distance = with(density) { SharedAxisDistance.roundToPx() }
        return ContentTransform(
            targetContentEnter = slideInHorizontally(motion.spatial()) { distance } + fadeIn(motion.spatial()),
            initialContentExit = slideOutHorizontally(motion.fastEffects()) { -distance } +
                fadeOut(motion.fastEffects()),
            sizeTransform = null,
        )
    }

    /** Back on the X axis: the previous screen comes in from the start side, the current one leaves toward the end. */
    fun sharedAxisBack(motion: PosMotion, density: Density): ContentTransform {
        val distance = with(density) { SharedAxisDistance.roundToPx() }
        return ContentTransform(
            targetContentEnter = sharedAxisBackEnter(motion, distance),
            initialContentExit = slideOutHorizontally(motion.fastEffects()) { distance } +
                fadeOut(motion.fastEffects()),
            sizeTransform = null,
        )
    }

    /** Between peers (login → home, tabs): no direction, the new screen settles in from slightly smaller. */
    fun fadeThrough(motion: PosMotion): ContentTransform = ContentTransform(
        targetContentEnter = fadeIn(motion.spatial()) +
            scaleIn(motion.spatial(), initialScale = FadeThroughInitialScale),
        initialContentExit = fadeOut(motion.fastEffects()),
        sizeTransform = null,
    )

    /**
     * A predictive back gesture. The gesture seeks this transition, so it runs on the linear `gesture` timeline: the
     * previous screen slides in from the start side and fades in while the leaving one shrinks to
     * [PredictiveBackScale], each in step with the swipe. The leaving screen stays opaque for most of the timeline and
     * fades out only at its end, when a completed gesture plays the remainder. With reduced motion it is simply
     * [sharedAxisBack], whose short fade follows the gesture.
     */
    fun predictiveBack(motion: PosMotion, density: Density): ContentTransform {
        if (motion.reduced) return sharedAxisBack(motion, density)
        val distance = with(density) { SharedAxisDistance.roundToPx() }
        return ContentTransform(
            targetContentEnter = slideInHorizontally(motion.gesture()) { -distance } + fadeIn(motion.gesture()),
            initialContentExit = scaleOut(motion.gesture(), targetScale = PredictiveBackScale) +
                fadeOut(predictiveBackExitFade(motion)),
            sizeTransform = null,
        )
    }

    /** Opaque through [PREDICTIVE_BACK_OPAQUE_SHARE] of the gesture timeline, then fading out. */
    private fun predictiveBackExitFade(motion: PosMotion): FiniteAnimationSpec<Float> = keyframes {
        durationMillis = motion.gestureMillis
        1f at (motion.gestureMillis * PREDICTIVE_BACK_OPAQUE_SHARE).toInt()
    }

    private fun sharedAxisBackEnter(motion: PosMotion, distance: Int): EnterTransition =
        slideInHorizontally(motion.spatial()) { -distance } + fadeIn(motion.spatial())
}
