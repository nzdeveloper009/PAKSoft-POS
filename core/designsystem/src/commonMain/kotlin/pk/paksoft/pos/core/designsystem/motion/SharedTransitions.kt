package pk.paksoft.pos.core.designsystem.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** The app root's `SharedTransitionLayout`; null in previews and tests. */
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> = compositionLocalOf { null }

/**
 * The animated scope the current screen enters and leaves with: the session gate's `AnimatedContent` for the shop
 * picker and login, `NavDisplay`'s scope for a signed-in screen. Null in previews and tests.
 */
val LocalPosAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }

/** Shared elements need both scopes and full motion; otherwise the element simply appears with its screen. */
fun sharedTransitionEnabled(hasSharedScope: Boolean, hasVisibilityScope: Boolean, reduced: Boolean): Boolean =
    hasSharedScope && hasVisibilityScope && !reduced

/** Shop picker card → login header (`sharedBounds`). */
fun shopCardKey(id: String): String = "shop-card-$id"

/** The shop's avatar on the picker card and the login header (`sharedElement`). */
fun shopAvatarKey(id: String): String = "shop-avatar-$id"

/** The shop's name on the picker card and the login header (`sharedElement`). */
fun shopNameKey(id: String): String = "shop-name-$id"

/**
 * Marks an element that travels to the element with the same [key] on the next screen (same content, e.g. a shop's
 * avatar), moving its bounds with the `spatial` spring. Does nothing without the scopes (previews) or with reduced
 * motion.
 */
@Composable
fun Modifier.posSharedElement(key: Any): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalPosAnimatedVisibilityScope.current
    val motion = PosTheme.motion
    val enabled = sharedTransitionEnabled(sharedScope != null, visibilityScope != null, motion.reduced)
    if (!enabled || sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        this@posSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = BoundsTransform { _, _ -> motion.spatial() },
        )
    }
}

/**
 * Marks a container whose bounds grow or shrink into the container with the same [key] on the next screen
 * (different content, e.g. a shop card becoming the login header): the bounds move with the `spatial` spring and the
 * two contents cross-fade with `effects`. Does nothing without the scopes (previews) or with reduced motion.
 */
@Composable
fun Modifier.posSharedBounds(key: Any): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalPosAnimatedVisibilityScope.current
    val motion = PosTheme.motion
    val enabled = sharedTransitionEnabled(sharedScope != null, visibilityScope != null, motion.reduced)
    if (!enabled || sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        this@posSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(motion.effects()),
            exit = fadeOut(motion.effects()),
            boundsTransform = BoundsTransform { _, _ -> motion.spatial() },
        )
    }
}
