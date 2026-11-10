package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Money and stock colours that Material 3 has no slot for. Errors use `colorScheme.error*`; success is primary. */
data class PosExtendedColors(
    val due: Color,
    val dueContainer: Color,
    val onDueContainer: Color,
    val paidContainer: Color,
    val onPaidContainer: Color,
    val chartIn: Color,
    val chartOut: Color,
)

val PosLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF0B6B4F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB6EFD4),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4C6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DB),
    onSecondaryContainer = Color(0xFF082018),
    tertiary = Color(0xFF7D5700),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFC55C),
    onTertiaryContainer = Color(0xFF281800),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6F7F2),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF6F7F2),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DE),
    onSurfaceVariant = Color(0xFF404943),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF2EC),
    surfaceContainer = Color(0xFFE8ECE6),
    surfaceContainerHigh = Color(0xFFE1E6E0),
    surfaceContainerHighest = Color(0xFFDBE0DA),
    surfaceDim = Color(0xFFD7DBD5),
    surfaceBright = Color(0xFFF6F7F2),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2),
    inverseSurface = Color(0xFF2E312F),
    inverseOnSurface = Color(0xFFEFF1EC),
    inversePrimary = Color(0xFF7EDAB3),
    scrim = Color(0xFF000000),
)

val PosDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF7EDAB3),
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFF9AF6CE),
    secondary = Color(0xFFB3CCBF),
    onSecondary = Color(0xFF1E352C),
    secondaryContainer = Color(0xFF34493F),
    onSecondaryContainer = Color(0xFFCEE9DB),
    tertiary = Color(0xFFF2B13E),
    onTertiary = Color(0xFF2A1A00),
    // Kept bright on purpose.
    tertiaryContainer = Color(0xFFF2B13E),
    onTertiaryContainer = Color(0xFF2A1A00),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1412),
    onBackground = Color(0xFFDFE4DF),
    surface = Color(0xFF0F1412),
    onSurface = Color(0xFFDFE4DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFC9C2),
    surfaceContainerLowest = Color(0xFF0A0F0D),
    surfaceContainerLow = Color(0xFF171D1A),
    surfaceContainer = Color(0xFF1B211E),
    surfaceContainerHigh = Color(0xFF252B28),
    surfaceContainerHighest = Color(0xFF303633),
    surfaceDim = Color(0xFF0F1412),
    surfaceBright = Color(0xFF353A37),
    outline = Color(0xFF89938C),
    outlineVariant = Color(0xFF3F4943),
    inverseSurface = Color(0xFFDFE4DF),
    inverseOnSurface = Color(0xFF2E312F),
    inversePrimary = Color(0xFF0B6B4F),
    scrim = Color(0xFF000000),
)

val PosLightExtendedColors = PosExtendedColors(
    due = Color(0xFF9A6100),
    dueContainer = Color(0xFFFFE2BA),
    onDueContainer = Color(0xFF2C1700),
    paidContainer = Color(0xFFB6EFD4),
    onPaidContainer = Color(0xFF002117),
    chartIn = PosLightColorScheme.primary,
    chartOut = PosLightColorScheme.tertiary,
)

val PosDarkExtendedColors = PosExtendedColors(
    due = Color(0xFFFFB95A),
    dueContainer = Color(0xFF4A3000),
    onDueContainer = Color(0xFFFFDDB5),
    paidContainer = Color(0xFF005139),
    onPaidContainer = Color(0xFF9AF6CE),
    chartIn = PosDarkColorScheme.primary,
    chartOut = PosDarkColorScheme.tertiary,
)
