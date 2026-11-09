package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalPosExtendedColors = staticCompositionLocalOf { PosLightExtendedColors }

@Composable
fun PosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPosExtendedColors provides if (darkTheme) PosDarkExtendedColors else PosLightExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) PosDarkColorScheme else PosLightColorScheme,
            typography = posTypography(),
            content = content,
        )
    }
}

object PosTheme {
    val colors: PosExtendedColors
        @Composable @ReadOnlyComposable get() = LocalPosExtendedColors.current
}
