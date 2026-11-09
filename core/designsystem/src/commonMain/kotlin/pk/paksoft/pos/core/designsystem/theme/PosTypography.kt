package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import pk.paksoft.pos.core.designsystem.resources.Res
import pk.paksoft.pos.core.designsystem.resources.nunito_variable

/** Material 3 type scale in Nunito (variable font, weights 400/600/700). */
@Composable
fun posTypography(): Typography {
    val family = FontFamily(
        Font(Res.font.nunito_variable, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
        Font(Res.font.nunito_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
        Font(Res.font.nunito_variable, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    )
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
    )
}
