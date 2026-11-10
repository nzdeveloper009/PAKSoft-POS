package pk.paksoft.pos.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
import pk.paksoft.pos.core.designsystem.component.PosButton
import pk.paksoft.pos.core.designsystem.component.PosButtonStyle
import pk.paksoft.pos.core.designsystem.component.PosLoadingIndicator
import pk.paksoft.pos.core.designsystem.component.PosTopBar
import pk.paksoft.pos.core.designsystem.component.ShopAvatar
import pk.paksoft.pos.core.designsystem.component.enterStagger
import pk.paksoft.pos.core.designsystem.component.posClickable
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.theme.PosLayout
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.designsystem.theme.ShopBrand
import pk.paksoft.pos.core.designsystem.theme.colors
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.asString
import pk.paksoft.pos.feature.settings.resources.Res
import pk.paksoft.pos.feature.settings.resources.more_logout
import pk.paksoft.pos.feature.settings.resources.more_refresh
import pk.paksoft.pos.feature.settings.resources.more_refresh_hint
import pk.paksoft.pos.feature.settings.resources.more_refreshed
import pk.paksoft.pos.feature.settings.resources.more_signed_in_as
import pk.paksoft.pos.feature.settings.resources.more_switch_shop
import pk.paksoft.pos.feature.settings.resources.more_switch_shop_hint
import pk.paksoft.pos.feature.settings.resources.more_title

/** The signed-in shop and user on the shop's colours, then the session actions. */
@Composable
fun MoreScreen(state: MoreUiState, onAction: (MoreAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            PosTopBar(title = stringResource(Res.string.more_title))
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = PosLayout.contentMaxWidth)
                        .fillMaxWidth()
                        .padding(start = PosSpacing.l, end = PosSpacing.l, top = PosSpacing.s, bottom = PosSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(PosSpacing.m),
                ) {
                    SignedInCard(state, Modifier.enterStagger(0))
                    val message = state.message
                    if (message != null && state.isMessageError) {
                        ErrorBanner(message = message.asString(), onRetry = { onAction(MoreAction.RefreshData) })
                    }
                    // A successful refresh reports in the card's own hint line, so nothing below it jumps.
                    val success = message?.takeUnless { state.isMessageError }
                    ActionCard(
                        icon = PosIcons.Refresh,
                        title = stringResource(Res.string.more_refresh),
                        hint = success?.asString() ?: stringResource(Res.string.more_refresh_hint),
                        hintIsStatus = success != null,
                        working = state.isRefreshing,
                        onClick = { onAction(MoreAction.RefreshData) },
                        modifier = Modifier.enterStagger(1),
                    )
                    ActionCard(
                        icon = PosIcons.Store,
                        title = stringResource(Res.string.more_switch_shop),
                        hint = stringResource(Res.string.more_switch_shop_hint),
                        onClick = { onAction(MoreAction.SwitchShop) },
                        modifier = Modifier.enterStagger(2),
                        showChevron = true,
                    )
                    PosButton(
                        text = stringResource(Res.string.more_logout),
                        onClick = { onAction(MoreAction.Logout) },
                        style = PosButtonStyle.Outlined,
                        icon = PosIcons.Logout,
                        modifier = Modifier.enterStagger(3).padding(top = PosSpacing.s).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** The shop (avatar, name, address) and the user signed in to it, on the shop's container colour. */
@Composable
private fun SignedInCard(state: MoreUiState, modifier: Modifier = Modifier) {
    // Built from the shop list's name, as on the shop picker and login, so the shop wears the same identity.
    val brand = remember(state.shopId, state.shopDisplayName) {
        ShopBrand.forShop(state.shopId, state.shopDisplayName)
    }
    val colors = brand.colors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.container)
            .padding(PosSpacing.l),
        verticalArrangement = Arrangement.spacedBy(PosSpacing.l),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PosSpacing.l),
        ) {
            ShopAvatar(brand, AvatarSize)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PosSpacing.xs)) {
                Text(
                    text = state.shopName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.shopHost,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // The person icon sits in a slot as wide as the avatar, so the user's name lines up under the shop's name.
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PosSpacing.l),
        ) {
            Box(modifier = Modifier.width(AvatarSize), contentAlignment = Alignment.Center) {
                Icon(
                    PosIcons.Person,
                    contentDescription = stringResource(Res.string.more_signed_in_as),
                    tint = colors.onContainer,
                )
            }
            Column {
                Text(
                    text = state.userName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.roleName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * A tappable list card: an icon on a tonal circle (a loading shape while [working]), a title and a hint. A hint that
 * reports the result of the card's action ([hintIsStatus]) shows in primary and is announced when it changes.
 */
@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hintIsStatus: Boolean = false,
    working: Boolean = false,
    showChevron: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .posClickable(enabled = !working, shape = MaterialTheme.shapes.large, role = Role.Button, onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(PosSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PosSpacing.l),
    ) {
        Box(
            modifier = Modifier
                .size(IconBadgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (working) {
                PosLoadingIndicator(
                    modifier = Modifier.size(IconBadgeSize),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            } else {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = if (hintIsStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (showChevron) {
            Icon(PosIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val AvatarSize = 56.dp
private val IconBadgeSize = 40.dp

private val PreviewState = MoreUiState(
    userName = "cashier1",
    roleName = "Admin",
    shopId = "smf",
    shopName = "Super madina Fan",
    shopDisplayName = "Super Madina Solar Fan",
    shopHost = "pbsf.org.pk/smf",
    message = UiText.Res(Res.string.more_refreshed),
)

@Preview
@Composable
private fun MoreScreenLightPreview() {
    PosTheme(darkTheme = false) { MoreScreen(state = PreviewState, onAction = {}) }
}

@Preview
@Composable
private fun MoreScreenDarkPreview() {
    PosTheme(darkTheme = true) { MoreScreen(state = PreviewState, onAction = {}) }
}

@Preview
@Composable
private fun MoreScreenErrorPreview() {
    PosTheme(darkTheme = false) {
        MoreScreen(
            state = PreviewState.copy(
                shopId = "mummyandme",
                shopName = "Mummy & Me",
                shopDisplayName = "Mummy & Me",
                shopHost = "pos.mummyandme.com.pk",
                message = UiText.Raw("No internet connection."),
                isMessageError = true,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun MoreScreenRefreshingDarkPreview() {
    PosTheme(darkTheme = true) {
        MoreScreen(
            state = PreviewState.copy(
                shopId = "bsf",
                shopName = "BSF",
                shopDisplayName = "BSF",
                shopHost = "pbsf.org.pk/bsf",
                message = null,
                isRefreshing = true,
            ),
            onAction = {},
        )
    }
}
