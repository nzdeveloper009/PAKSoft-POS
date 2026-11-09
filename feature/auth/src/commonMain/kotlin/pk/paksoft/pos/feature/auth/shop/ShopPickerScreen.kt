package pk.paksoft.pos.feature.auth.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.feature.auth.resources.Res
import pk.paksoft.pos.feature.auth.resources.shop_dev_badge
import pk.paksoft.pos.feature.auth.resources.shop_picker_subtitle
import pk.paksoft.pos.feature.auth.resources.shop_picker_title

@Composable
fun ShopPickerScreen(state: ShopPickerUiState, onAction: (ShopPickerAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(PosSpacing.l),
                verticalArrangement = Arrangement.spacedBy(PosSpacing.m),
            ) {
                item(key = "header") {
                    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.xs)) {
                        Text(stringResource(Res.string.shop_picker_title), style = MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(Res.string.shop_picker_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(state.shops, key = { it.id }) { shop ->
                    Card(onClick = { onAction(ShopPickerAction.ShopClicked(shop.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(PosSpacing.l),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PosSpacing.s),
                        ) {
                            Text(shop.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (shop.isDevelopment) {
                                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                                    Text(
                                        stringResource(Res.string.shop_dev_badge),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = PosSpacing.s, vertical = PosSpacing.xs),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ShopPickerScreenPreview() {
    PosTheme {
        ShopPickerScreen(
            state = ShopPickerUiState(
                listOf(
                    ShopItem("smf", "Super Madina Solar Fan", false),
                    ShopItem("bsf", "BSF", false),
                    ShopItem("local", "Local API (dev)", true),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ShopPickerScreenEmptyPreview() {
    PosTheme { ShopPickerScreen(state = ShopPickerUiState(), onAction = {}) }
}
