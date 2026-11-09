package pk.paksoft.pos.app.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.app.resources.Res
import pk.paksoft.pos.app.resources.coming_soon
import pk.paksoft.pos.app.resources.nav_dashboard
import pk.paksoft.pos.core.designsystem.component.PosTopBar
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** Placeholder for a tab whose feature isn't built yet. */
@Composable
fun ComingSoonScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            PosTopBar(title = title)
            Box(modifier = Modifier.fillMaxSize().padding(PosSpacing.xl), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(Res.string.coming_soon),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ComingSoonScreenPreview() {
    PosTheme {
        ComingSoonScreen(title = stringResource(Res.string.nav_dashboard))
    }
}
