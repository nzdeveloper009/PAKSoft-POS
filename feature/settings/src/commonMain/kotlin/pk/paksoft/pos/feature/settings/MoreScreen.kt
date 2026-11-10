package pk.paksoft.pos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
import pk.paksoft.pos.core.designsystem.component.PosButton
import pk.paksoft.pos.core.designsystem.component.PosButtonStyle
import pk.paksoft.pos.core.designsystem.component.PosTopBar
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.asString
import pk.paksoft.pos.feature.settings.resources.Res
import pk.paksoft.pos.feature.settings.resources.more_logout
import pk.paksoft.pos.feature.settings.resources.more_refresh
import pk.paksoft.pos.feature.settings.resources.more_refreshed
import pk.paksoft.pos.feature.settings.resources.more_shop
import pk.paksoft.pos.feature.settings.resources.more_signed_in_as
import pk.paksoft.pos.feature.settings.resources.more_switch_shop
import pk.paksoft.pos.feature.settings.resources.more_title

@Composable
fun MoreScreen(state: MoreUiState, onAction: (MoreAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            PosTopBar(title = stringResource(Res.string.more_title))
            Column(
                modifier = Modifier.fillMaxWidth().padding(PosSpacing.l),
                verticalArrangement = Arrangement.spacedBy(PosSpacing.m),
            ) {
                Text(
                    stringResource(Res.string.more_signed_in_as, state.userName, state.roleName),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.more_shop, state.shopName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.message?.let { message ->
                    if (state.isMessageError) {
                        ErrorBanner(message = message.asString(), onRetry = { onAction(MoreAction.RefreshData) })
                    } else {
                        Text(
                            message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
                PosButton(
                    text = stringResource(Res.string.more_refresh),
                    onClick = { onAction(MoreAction.RefreshData) },
                    style = PosButtonStyle.Outlined,
                    enabled = !state.isRefreshing,
                    modifier = Modifier.fillMaxWidth(),
                )
                PosButton(
                    text = stringResource(Res.string.more_switch_shop),
                    onClick = { onAction(MoreAction.SwitchShop) },
                    style = PosButtonStyle.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = { onAction(MoreAction.Logout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.more_logout)) }
            }
        }
    }
}

@Preview
@Composable
private fun MoreScreenPreview() {
    PosTheme {
        MoreScreen(
            state = MoreUiState(
                userName = "cashier1",
                roleName = "Admin",
                shopName = "Super madina Fan",
                message = UiText.Res(Res.string.more_refreshed),
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun MoreScreenErrorPreview() {
    PosTheme {
        MoreScreen(
            state = MoreUiState(
                userName = "cashier1",
                roleName = "Admin",
                shopName = "Super madina Fan",
                message = UiText.Raw("No internet connection."),
                isMessageError = true,
            ),
            onAction = {},
        )
    }
}
