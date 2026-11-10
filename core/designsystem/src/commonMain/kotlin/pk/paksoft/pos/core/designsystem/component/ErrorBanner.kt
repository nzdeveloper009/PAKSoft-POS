package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.resources.Res
import pk.paksoft.pos.core.designsystem.resources.retry
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/**
 * Inline error message on the error container, with an error mark so it doesn't rely on colour alone.
 * [onRetry] adds a tonal retry button; its label defaults to the `retry` string resource.
 */
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
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(
                start = PosSpacing.l,
                end = if (onRetry != null) PosSpacing.s else PosSpacing.l,
                top = PosSpacing.m,
                bottom = PosSpacing.m,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PosSpacing.m),
        ) {
            Icon(PosIcons.Error, contentDescription = null)
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onRetry != null) PosButton(text = retryLabel, onClick = onRetry, style = PosButtonStyle.Tonal)
        }
    }
}

@Preview
@Composable
private fun ErrorBannerLightPreview() {
    PosPreview(darkTheme = false) { ErrorBanner(message = "No internet connection.", onRetry = {}) }
}

@Preview
@Composable
private fun ErrorBannerDarkPreview() {
    PosPreview(darkTheme = true) { ErrorBanner(message = "No internet connection.", onRetry = {}) }
}

@Preview
@Composable
private fun ErrorBannerNoRetryPreview() {
    PosPreview(darkTheme = false) { ErrorBanner(message = "The username/email or password is incorrect.") }
}
