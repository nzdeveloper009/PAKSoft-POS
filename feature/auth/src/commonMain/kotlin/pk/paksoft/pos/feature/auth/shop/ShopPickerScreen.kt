package pk.paksoft.pos.feature.auth.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.component.PosBrandMark
import pk.paksoft.pos.core.designsystem.component.ShopAvatar
import pk.paksoft.pos.core.designsystem.component.enterStagger
import pk.paksoft.pos.core.designsystem.component.posClickable
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.motion.PosSharedResize
import pk.paksoft.pos.core.designsystem.motion.posSharedBounds
import pk.paksoft.pos.core.designsystem.motion.posSharedElement
import pk.paksoft.pos.core.designsystem.motion.shopAvatarKey
import pk.paksoft.pos.core.designsystem.motion.shopCardKey
import pk.paksoft.pos.core.designsystem.motion.shopNameKey
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.designsystem.theme.ShopBrand
import pk.paksoft.pos.feature.auth.resources.Res
import pk.paksoft.pos.feature.auth.resources.shop_dev_badge
import pk.paksoft.pos.feature.auth.resources.shop_picker_brand
import pk.paksoft.pos.feature.auth.resources.shop_picker_subtitle
import pk.paksoft.pos.feature.auth.resources.shop_picker_title

/**
 * The shops this build can sign in to, each wearing its identity. A tapped card grows into the login header: the
 * card's bounds, avatar and name are shared with the login screen.
 *
 * The list is short and fixed (three shops, six in debug builds), so it is a plain scrolling column: its sections
 * stagger in once and don't replay as rows scroll back into view.
 */
@Composable
fun ShopPickerScreen(state: ShopPickerUiState, onAction: (ShopPickerAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = PosSpacing.l, vertical = PosSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(PosSpacing.m),
            ) {
                Header(Modifier.enterStagger(0).padding(bottom = PosSpacing.m))
                state.shops.forEachIndexed { index, shop ->
                    key(shop.id) {
                        ShopCard(
                            shop = shop,
                            onClick = { onAction(ShopPickerAction.ShopClicked(shop.id)) },
                            modifier = Modifier.enterStagger(index + 1),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        PosBrandMark(stringResource(Res.string.shop_picker_brand))
        Spacer(Modifier.height(PosSpacing.xl))
        Text(
            stringResource(Res.string.shop_picker_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PosSpacing.s))
        Text(
            stringResource(Res.string.shop_picker_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShopCard(shop: ShopItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brand = remember(shop.id, shop.name) { ShopBrand.forShop(shop.id, shop.name) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .posSharedBounds(shopCardKey(shop.id), PosSharedResize.Remeasure)
            .posClickable(shape = MaterialTheme.shapes.extraLarge, role = Role.Button, onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = PosSpacing.m, top = PosSpacing.m, bottom = PosSpacing.m, end = PosSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PosSpacing.l),
    ) {
        ShopAvatar(brand, AvatarSize, Modifier.posSharedElement(shopAvatarKey(shop.id)))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PosSpacing.xs)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PosSpacing.s),
            ) {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .posSharedBounds(shopNameKey(shop.id), PosSharedResize.Scale),
                )
                if (shop.isDevelopment) DevBadge()
            }
            Text(
                text = shop.host,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(PosIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Marks the debug-only shops (Demo, Staging, Local API). */
@Composable
private fun DevBadge() {
    Text(
        text = stringResource(Res.string.shop_dev_badge),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = PosSpacing.s),
    )
}

private val MaxContentWidth = 420.dp
private val AvatarSize = 52.dp

private val PreviewShops = listOf(
    ShopItem("smf", "Super Madina Solar Fan", "pbsf.org.pk/smf", isDevelopment = false),
    ShopItem("bsf", "BSF", "pbsf.org.pk/bsf", isDevelopment = false),
    ShopItem("mummyandme", "Mummy & Me", "pos.mummyandme.com.pk", isDevelopment = false),
    ShopItem("demo", "Demo shop", "demo.paksoft.invalid", isDevelopment = true),
    ShopItem("local", "Local API (dev)", "10.0.2.2:8010", isDevelopment = true),
)

@Preview
@Composable
private fun ShopPickerScreenLightPreview() {
    PosTheme(darkTheme = false) { ShopPickerScreen(state = ShopPickerUiState(PreviewShops), onAction = {}) }
}

@Preview
@Composable
private fun ShopPickerScreenDarkPreview() {
    PosTheme(darkTheme = true) { ShopPickerScreen(state = ShopPickerUiState(PreviewShops), onAction = {}) }
}
