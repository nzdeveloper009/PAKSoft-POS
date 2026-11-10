package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The only source of animation specs in the app. The first six springs equal
 * `MotionScheme.expressive()`. With [reduced] on, spatial specs snap and effects become a short tween.
 */
class PosMotion(val reduced: Boolean) {
    val staggerStepMillis: Int = 50
    val enterOffset: Dp = 18.dp

    /** Delay between neighbouring digits of a rolling number, right-most digit first. */
    val rollingStaggerMillis: Int = 18

    /** Length of a gesture-driven timeline ([gesture]); 0 with [reduced] on, where the spec snaps. */
    val gestureMillis: Int = if (reduced) 0 else GESTURE_MILLIS

    fun <T> fastSpatial(): FiniteAnimationSpec<T> = spatialSpring(0.6f, 800f)
    fun <T> spatial(): FiniteAnimationSpec<T> = spatialSpring(0.8f, 380f)
    fun <T> slowSpatial(): FiniteAnimationSpec<T> = spatialSpring(0.8f, 200f)
    fun <T> fastEffects(): FiniteAnimationSpec<T> = effectsSpring(1.0f, 3800f)
    fun <T> effects(): FiniteAnimationSpec<T> = effectsSpring(1.0f, 1600f)
    fun <T> slowEffects(): FiniteAnimationSpec<T> = effectsSpring(1.0f, 800f)

    /** Cart badge and sale-complete badge only. */
    fun <T> bouncy(): FiniteAnimationSpec<T> = spatialSpring(0.45f, 500f)

    /** Numbers counting up. Decorative: callers skip the count when [reduced]. */
    fun <T> countUp(): FiniteAnimationSpec<T> = effectsSpring(1.0f, 120f)

    /**
     * A transition a gesture seeks, such as predictive back: linear, so the animation follows the finger one to one
     * (a spring would do most of its travel in the first part of the swipe), and short, since only the part left
     * after the user lets go plays in real time. Snaps with [reduced] on.
     */
    fun <T> gesture(): FiniteAnimationSpec<T> = if (reduced) snap() else tween(gestureMillis, easing = LinearEasing)

    /** One sweep of the skeleton shimmer. Callers draw a static block instead when [reduced]. */
    fun <T> shimmer(): InfiniteRepeatableSpec<T> =
        infiniteRepeatable(tween(SHIMMER_MILLIS, easing = LinearEasing), RepeatMode.Restart)

    private fun <T> spatialSpring(damping: Float, stiffness: Float): FiniteAnimationSpec<T> =
        if (reduced) snap() else spring(damping, stiffness)

    private fun <T> effectsSpring(damping: Float, stiffness: Float): FiniteAnimationSpec<T> =
        if (reduced) tween(REDUCED_FADE_MILLIS) else spring(damping, stiffness)

    private companion object {
        const val REDUCED_FADE_MILLIS = 120
        const val SHIMMER_MILLIS = 1200
        const val GESTURE_MILLIS = 300
    }
}
