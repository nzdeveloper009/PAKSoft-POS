package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.theme.PosTheme

@Composable
fun PosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled && !loading) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun PosOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Preview
@Composable
private fun PosButtonPreview() {
    PosTheme {
        PosButton(text = "Log in", onClick = {})
    }
}

@Preview
@Composable
private fun PosOutlinedButtonPreview() {
    PosTheme {
        PosOutlinedButton(text = "Change shop", onClick = {})
    }
}
