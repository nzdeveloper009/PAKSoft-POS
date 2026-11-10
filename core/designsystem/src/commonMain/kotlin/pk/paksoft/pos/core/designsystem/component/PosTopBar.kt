package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.icon.PosIcons

/** A screen's top bar on the background colour, with a bold headline title. Actions are [PosIconButton]s. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        modifier = modifier,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun PosTopBarSample() {
    PosTopBar(
        title = "More",
        actions = { PosIconButton(icon = PosIcons.Refresh, contentDescription = "Refresh", onClick = {}) },
    )
}

@Preview
@Composable
private fun PosTopBarLightPreview() {
    PosPreview(darkTheme = false) { PosTopBarSample() }
}

@Preview
@Composable
private fun PosTopBarDarkPreview() {
    PosPreview(darkTheme = true) { PosTopBarSample() }
}
