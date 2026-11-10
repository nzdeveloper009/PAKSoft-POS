package pk.paksoft.pos.core.designsystem.theme

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
            motionScheme = MotionScheme.expressive(),
            shapes = PosShapes,
            typography = typography,
            content = content,
        )
    }
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
