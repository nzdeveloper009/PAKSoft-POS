package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.ShopBrand
import pk.paksoft.pos.core.designsystem.theme.colors
import pk.paksoft.pos.core.designsystem.theme.polygon

/**
 * A shop's mark: its initials on its shape in its colour. Decorative (hidden from screen readers) because the shop
 * name is always shown next to it. The initials stay at 30% of [size] whatever the user's font scale, and scale with
 * the avatar when a shared transition (`posSharedElement`) measures it at an in-between size.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShopAvatar(brand: ShopBrand, size: Dp, modifier: Modifier = Modifier) {
    val colors = brand.colors()
    val density = LocalDensity.current
    val fontSize = with(density) { (size * INITIALS_FRACTION).toSp() }
    val sizePx = with(density) { size.toPx() }
    Box(
        modifier = modifier
            .size(size)
            .background(colors.color, brand.shape.polygon().toShape())
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = brand.initials,
            color = colors.onColor,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = fontSize,
                lineHeight = fontSize,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.scaleWithAvatar(sizePx),
        )
    }
}

/**
 * Measures the initials at their own size and scales them by the avatar's measured width over [nominalPx]: the
 * avatar box passes its width down as the maximum.
 */
private fun Modifier.scaleWithAvatar(nominalPx: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(Constraints())
    val scale = if (constraints.hasBoundedWidth) initialsScale(nominalPx, constraints.maxWidth) else 1f
    layout(placeable.width, placeable.height) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
        }
    }
}

/** How much the initials scale when the avatar is [measuredPx] wide instead of its own [nominalPx]. */
internal fun initialsScale(nominalPx: Float, measuredPx: Int): Float =
    if (nominalPx > 0f) measuredPx / nominalPx else 1f

private const val INITIALS_FRACTION = 0.3f

@Composable
private fun ShopAvatarSample() {
    Row(horizontalArrangement = Arrangement.spacedBy(PosSpacing.l), verticalAlignment = Alignment.CenterVertically) {
        ShopAvatar(ShopBrand.forShop("smf", "Super Madina Fan"), 52.dp)
        ShopAvatar(ShopBrand.forShop("bsf", "BSF"), 52.dp)
        ShopAvatar(ShopBrand.forShop("mummyandme", "Mummy & Me"), 84.dp)
        ShopAvatar(ShopBrand.forShop("demo", "Demo shop"), 40.dp)
        ShopAvatar(ShopBrand.forShop("local", "Local API"), 40.dp)
    }
}

@Preview
@Composable
private fun ShopAvatarLightPreview() {
    PosPreview(darkTheme = false) { ShopAvatarSample() }
}

@Preview
@Composable
private fun ShopAvatarDarkPreview() {
    PosPreview(darkTheme = true) { ShopAvatarSample() }
}
