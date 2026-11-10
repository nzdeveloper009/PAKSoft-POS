package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pk.paksoft.pos.core.designsystem.resources.Res
import pk.paksoft.pos.core.designsystem.resources.nunito_variable

/** Money text styles: the matching scale style plus tabular figures. */
data class PosTypographyExtras(
    val moneyLarge: TextStyle,
    val moneyMedium: TextStyle,
    val moneySmall: TextStyle,
)

private const val TABULAR_FIGURES = "tnum"

@Composable
private fun nunito(): FontFamily = FontFamily(
    listOf(400, 500, 600, 700, 800, 900).map { weight ->
        Font(
            Res.font.nunito_variable,
            FontWeight(weight),
            variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
        )
    },
)

/** Material 3 type scale in Nunito (variable font, weights 400-900). */
@Composable
fun posTypography(): Typography {
    val family = nunito()
    val base = Typography()
    fun TextStyle.nunito(weight: Int, size: Int? = null, line: Int? = null): TextStyle = copy(
        fontFamily = family,
        fontWeight = FontWeight(weight),
        fontSize = size?.sp ?: fontSize,
        lineHeight = line?.sp ?: lineHeight,
    )
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.nunito(900, 36, 44),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.nunito(900, 28, 36),
        headlineSmall = base.headlineSmall.nunito(800, 24, 32),
        titleLarge = base.titleLarge.nunito(800, 22, 28),
        titleMedium = base.titleMedium.nunito(800, 16, 24),
        titleSmall = base.titleSmall.nunito(700, 14, 20),
        bodyLarge = base.bodyLarge.nunito(500, 16, 24),
        bodyMedium = base.bodyMedium.nunito(500, 14, 20),
        bodySmall = base.bodySmall.nunito(500, 12, 16),
        labelLarge = base.labelLarge.nunito(700, 15, 20),
        labelMedium = base.labelMedium.nunito(700, 13, 16),
        labelSmall = base.labelSmall.nunito(700, 11, 16),
    )
}

@Composable
internal fun posTypographyExtras(typography: Typography): PosTypographyExtras = PosTypographyExtras(
    moneyLarge = typography.displaySmall.copy(fontFeatureSettings = TABULAR_FIGURES),
    moneyMedium = typography.titleLarge.copy(fontFeatureSettings = TABULAR_FIGURES),
    moneySmall = typography.titleMedium.copy(fontFeatureSettings = TABULAR_FIGURES),
)
