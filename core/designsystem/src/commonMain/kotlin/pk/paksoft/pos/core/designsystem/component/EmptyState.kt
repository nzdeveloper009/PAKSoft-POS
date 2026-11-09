package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(PosSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PosSpacing.s, Alignment.CenterVertically),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    PosTheme {
        EmptyState(title = "Dashboard", message = "Coming in the next update.")
    }
}
