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
import pk.paksoft.pos.core.designsystem.theme.PosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Preview
@Composable
private fun PosTopBarPreview() {
    PosTheme {
        PosTopBar(title = "More")
    }
}
