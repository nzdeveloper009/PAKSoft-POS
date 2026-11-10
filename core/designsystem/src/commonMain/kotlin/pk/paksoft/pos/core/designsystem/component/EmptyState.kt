package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/** A screen with nothing to show yet: [icon] on a four-leaf clover, a [title] and a short [message]. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyState(title: String, message: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(PosSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PosSpacing.s, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(MARK_SIZE)
                .background(MaterialTheme.colorScheme.secondaryContainer, MaterialShapes.Clover4Leaf.toShape()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
        Spacer(Modifier.height(PosSpacing.s))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val MARK_SIZE = 112.dp
private val ICON_SIZE = 44.dp

@Preview
@Composable
private fun EmptyStateLightPreview() {
    PosPreview(darkTheme = false) {
        EmptyState(title = "Sales", message = "Coming in the next update.", icon = PosIcons.Sales)
    }
}

@Preview
@Composable
private fun EmptyStateDarkPreview() {
    PosPreview(darkTheme = true) {
        EmptyState(title = "Sales", message = "Coming in the next update.", icon = PosIcons.Sales)
    }
}
