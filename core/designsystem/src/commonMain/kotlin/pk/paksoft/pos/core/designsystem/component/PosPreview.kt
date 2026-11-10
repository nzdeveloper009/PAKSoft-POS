package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** Wraps a component preview in [PosTheme] on the background colour, so light and dark previews match the app. */
@Composable
internal fun PosPreview(darkTheme: Boolean, content: @Composable () -> Unit) {
    PosTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(PosSpacing.l)) { content() }
        }
    }
}
