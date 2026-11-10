package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/**
 * The app's icon button: a Material icon button (48 dp touch target, ripple) that squeezes to [PosPressScale.icon]
 * while pressed and springs back on release. Disabled, it ignores taps and dims its icon the Material way.
 *
 * [tint] colours the icon; by default it takes the surrounding content colour (a top bar's action colour, a text
 * field's trailing icon colour). Give [contentDescription] unless a visible label next to it already names the action.
 */
@Composable
fun PosIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, PosPressScale.icon),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
        interactionSource = interactionSource,
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@Composable
private fun PosIconButtonSample() {
    Row(horizontalArrangement = Arrangement.spacedBy(PosSpacing.s)) {
        PosIconButton(icon = PosIcons.Back, contentDescription = "Back", onClick = {})
        PosIconButton(
            icon = PosIcons.Refresh,
            contentDescription = "Refresh",
            onClick = {},
            tint = MaterialTheme.colorScheme.primary,
        )
        PosIconButton(icon = PosIcons.Visibility, contentDescription = "Show password", onClick = {}, enabled = false)
    }
}

@Preview
@Composable
private fun PosIconButtonLightPreview() {
    PosPreview(darkTheme = false) { PosIconButtonSample() }
}

@Preview
@Composable
private fun PosIconButtonDarkPreview() {
    PosPreview(darkTheme = true) { PosIconButtonSample() }
}
