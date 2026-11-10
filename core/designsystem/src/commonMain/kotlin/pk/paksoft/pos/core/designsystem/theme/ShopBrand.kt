package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon

/** The shape a shop wears on its avatar, header and picker card. */
enum class ShopShape { SUNNY, COOKIE_9, FLOWER, CLOVER_4, SQUARE }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ShopShape.polygon(): RoundedPolygon = when (this) {
    ShopShape.SUNNY -> MaterialShapes.Sunny
    ShopShape.COOKIE_9 -> MaterialShapes.Cookie9Sided
    ShopShape.FLOWER -> MaterialShapes.Flower
    ShopShape.CLOVER_4 -> MaterialShapes.Clover4Leaf
    ShopShape.SQUARE -> MaterialShapes.Square
}

data class ShopBrandColors(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
)

/** A shop's identity: initials, shape and a colour pair per theme. Buttons, charts and money stay ledger green. */
data class ShopBrand(
    val initials: String,
    val shape: ShopShape,
    val light: ShopBrandColors,
    val dark: ShopBrandColors,
) {
    companion object {
        fun forShop(id: String, displayName: String): ShopBrand = when (id) {
            "smf" -> ShopBrand(
                initials = "SM",
                shape = ShopShape.SUNNY,
                light = ShopBrandColors(Color(0xFFE8890C), Color(0xFF2A1600), Color(0xFFFFDDB5), Color(0xFF2A1600)),
                dark = ShopBrandColors(Color(0xFFFFB45C), Color(0xFF2A1600), Color(0xFF4A2C00), Color(0xFFFFDDB5)),
            )
            "bsf" -> ShopBrand(
                initials = "BS",
                shape = ShopShape.COOKIE_9,
                light = ShopBrandColors(Color(0xFF2F5DA8), Color(0xFFFFFFFF), Color(0xFFD8E2FF), Color(0xFF001A41)),
                dark = ShopBrandColors(Color(0xFFA9C4FF), Color(0xFF00214F), Color(0xFF1B3A70), Color(0xFFD8E2FF)),
            )
            "mummyandme" -> ShopBrand(
                initials = "M&",
                shape = ShopShape.FLOWER,
                light = ShopBrandColors(Color(0xFFC2436E), Color(0xFFFFFFFF), Color(0xFFFFD9E2), Color(0xFF3E0020)),
                dark = ShopBrandColors(Color(0xFFFFB1C8), Color(0xFF4A0025), Color(0xFF5E1133), Color(0xFFFFD9E2)),
            )
            "demo" -> ShopBrand(
                initials = "DM",
                shape = ShopShape.CLOVER_4,
                light = ShopBrandColors(Color(0xFF0B6B4F), Color(0xFFFFFFFF), Color(0xFFB6EFD4), Color(0xFF002117)),
                dark = ShopBrandColors(Color(0xFF7EDAB3), Color(0xFF003827), Color(0xFF005139), Color(0xFF9AF6CE)),
            )
            else -> ShopBrand(
                initials = displayName.filter { it.isLetterOrDigit() }.take(2).uppercase(),
                shape = ShopShape.SQUARE,
                light = ShopBrandColors(Color(0xFF5D6670), Color(0xFFFFFFFF), Color(0xFFDDE3EA), Color(0xFF161C22)),
                dark = ShopBrandColors(Color(0xFFAEB8C2), Color(0xFF1A2128), Color(0xFF3A434C), Color(0xFFDDE3EA)),
            )
        }
    }
}

/** The brand colours for the current theme. */
@Composable
@ReadOnlyComposable
fun ShopBrand.colors(): ShopBrandColors = if (PosTheme.isDark) dark else light
