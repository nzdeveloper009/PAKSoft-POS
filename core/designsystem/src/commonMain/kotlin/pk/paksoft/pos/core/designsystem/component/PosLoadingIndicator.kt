package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/** The Material 3 Expressive morphing loading shape, in the surrounding content colour unless [color] is given. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosLoadingIndicator(modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    LoadingIndicator(modifier = modifier, color = color)
}

@Composable
private fun PosLoadingIndicatorSample() {
    Row(horizontalArrangement = Arrangement.spacedBy(PosSpacing.l)) {
        PosLoadingIndicator(color = MaterialTheme.colorScheme.primary)
        PosLoadingIndicator(color = MaterialTheme.colorScheme.tertiary)
    }
}

@Preview
@Composable
private fun PosLoadingIndicatorLightPreview() {
    PosPreview(darkTheme = false) { PosLoadingIndicatorSample() }
}

@Preview
@Composable
private fun PosLoadingIndicatorDarkPreview() {
    PosPreview(darkTheme = true) { PosLoadingIndicatorSample() }
}
