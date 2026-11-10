package pk.paksoft.pos.app.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.app.resources.Res
import pk.paksoft.pos.app.resources.coming_soon
import pk.paksoft.pos.core.designsystem.component.EmptyState
import pk.paksoft.pos.core.designsystem.component.PosTopBar
import pk.paksoft.pos.core.designsystem.component.enterStagger
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** Placeholder for a tab whose feature isn't built yet: the tab's icon and name, and "coming soon". */
@Composable
fun ComingSoonScreen(destination: TopLevelDestination) {
    val title = stringResource(destination.label)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            PosTopBar(title = title)
            EmptyState(
                title = title,
                message = stringResource(Res.string.coming_soon),
                icon = destination.icon,
                modifier = Modifier.enterStagger(0),
            )
        }
    }
}

@Preview
@Composable
private fun ComingSoonScreenLightPreview() {
    PosTheme(darkTheme = false) { ComingSoonScreen(TopLevelDestination.SALES) }
}

@Preview
@Composable
private fun ComingSoonScreenDarkPreview() {
    PosTheme(darkTheme = true) { ComingSoonScreen(TopLevelDestination.SALES) }
}
