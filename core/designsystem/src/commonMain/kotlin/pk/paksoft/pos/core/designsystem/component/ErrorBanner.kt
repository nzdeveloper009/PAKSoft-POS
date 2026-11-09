package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.resources.Res
import pk.paksoft.pos.core.designsystem.resources.retry
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** Inline error message. [onRetry] adds a retry action; its label defaults to the `retry` string resource. */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = stringResource(Res.string.retry),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PosSpacing.m, vertical = PosSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PosSpacing.s),
        ) {
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onRetry != null) TextButton(onClick = onRetry) { Text(retryLabel) }
        }
    }
}

@Preview
@Composable
private fun ErrorBannerPreview() {
    PosTheme {
        ErrorBanner(message = "No internet connection.", onRetry = {}, modifier = Modifier.padding(16.dp))
    }
}
