package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** A whole screen waiting for its first data: the morphing loading shape, centred. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PosLoadingIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun LoadingStateLightPreview() {
    PosPreview(darkTheme = false) { LoadingState() }
}

@Preview
@Composable
private fun LoadingStateDarkPreview() {
    PosPreview(darkTheme = true) { LoadingState() }
}
