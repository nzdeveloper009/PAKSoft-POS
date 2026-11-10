package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalPosExtendedColors = staticCompositionLocalOf { PosLightExtendedColors }
/** Internal so modifier nodes can read it with `currentValueOf`; everything else uses [PosTheme.motion]. */
internal val LocalPosMotion = staticCompositionLocalOf { PosMotion(reduced = false) }
private val LocalPosIsDark = staticCompositionLocalOf { false }
private val LocalPosTypographyExtras = staticCompositionLocalOf { posTypographyExtras(Typography()) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val reduced = rememberReduceMotion()
    val motion = remember(reduced) { PosMotion(reduced) }
    val motionScheme = remember(motion) { posMotionScheme(motion) }
    val typography = posTypography()
    val extras = remember(typography) { posTypographyExtras(typography) }
    CompositionLocalProvider(
        LocalPosExtendedColors provides if (darkTheme) PosDarkExtendedColors else PosLightExtendedColors,
        LocalPosMotion provides motion,
        LocalPosIsDark provides darkTheme,
        LocalPosTypographyExtras provides extras,
    ) {
        MaterialExpressiveTheme(
            colorScheme = if (darkTheme) PosDarkColorScheme else PosLightColorScheme,
            motionScheme = motionScheme,
            shapes = PosShapes,
            typography = typography,
            content = content,
        )
    }
}

/**
 * The motion scheme Material 3 components use (button shape morph, loading indicator, nav indicator).
 * Expressive springs normally; with reduced motion it hands out [PosMotion]'s specs, so spatial motion snaps and
 * effects become the short tween, as in our own components.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun posMotionScheme(motion: PosMotion): MotionScheme =
    if (motion.reduced) ReducedMotionScheme(motion) else MotionScheme.expressive()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class ReducedMotionScheme(private val motion: PosMotion) : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = motion.spatial()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = motion.fastSpatial()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = motion.slowSpatial()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = motion.effects()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = motion.fastEffects()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = motion.slowEffects()
}

object PosTheme {
    val colors: PosExtendedColors
        @Composable @ReadOnlyComposable get() = LocalPosExtendedColors.current
    val typography: PosTypographyExtras
        @Composable @ReadOnlyComposable get() = LocalPosTypographyExtras.current
    val motion: PosMotion
        @Composable @ReadOnlyComposable get() = LocalPosMotion.current
    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalPosIsDark.current
}
