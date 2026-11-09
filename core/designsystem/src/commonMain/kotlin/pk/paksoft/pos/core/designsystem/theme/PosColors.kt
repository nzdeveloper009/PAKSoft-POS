package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Status and POS payment colors that Material 3 has no slot for. Values mirror the web app. */
data class PosExtendedColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val payCard: Color,
    val payCash: Color,
    val payPaypal: Color,
    val payDraft: Color,
    val payCheque: Color,
    val payGiftCard: Color,
    val payDeposit: Color,
    val payCancel: Color,
    val payRecent: Color,
)

val PosLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF7C5CC4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4E6FC),
    onPrimaryContainer = Color(0xFF6244A6),
    background = Color(0xFFFAFBFE),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xFFDC3545),
    onError = Color(0xFFFFFFFF),
)

val PosDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA48AE6),
    onPrimary = Color(0xFF2A1A52),
    primaryContainer = Color(0xFF2A2342),
    onPrimaryContainer = Color(0xFFE4E6FC),
    background = Color(0xFF14121C),
    onBackground = Color(0xFFE6E1EC),
    surface = Color(0xFF1C1928),
    onSurface = Color(0xFFE6E1EC),
    error = Color(0xFFFF6B7A),
    onError = Color(0xFF3B0A10),
)

val PosLightExtendedColors = PosExtendedColors(
    success = Color(0xFF28A745),
    warning = Color(0xFFFFC107),
    danger = Color(0xFFDC3545),
    info = Color(0xFF17A2B8),
    payCard = Color(0xFF0984E3),
    payCash = Color(0xFF00CEC9),
    payPaypal = Color(0xFF213170),
    payDraft = Color(0xFFE28D02),
    payCheque = Color(0xFFFD7272),
    payGiftCard = Color(0xFF5F27CD),
    payDeposit = Color(0xFFB33771),
    payCancel = Color(0xFFD63031),
    payRecent = Color(0xFFFFC107),
)

val PosDarkExtendedColors = PosLightExtendedColors.copy(
    success = Color(0xFF5DD17A),
    warning = Color(0xFFFFD54F),
    danger = Color(0xFFFF6B7A),
    info = Color(0xFF4FC3D6),
)
