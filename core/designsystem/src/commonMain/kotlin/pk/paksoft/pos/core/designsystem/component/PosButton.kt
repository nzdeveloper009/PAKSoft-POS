package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * - [Filled]: a screen's main action.
 * - [Charge]: marigold, only for the actions that take money (Charge, New sale).
 * - [Tonal]: secondary actions (Share receipt, Retry).
 * - [Outlined]: alternatives next to a main action (Switch shop, Log out).
 * - [Text]: the least important action (Cancel).
 */
enum class PosButtonStyle { Filled, Charge, Tonal, Outlined, Text }

/**
 * The app's button. Fully round at rest; while pressed its corners square off (`ButtonDefaults.shapes()`) and it
 * squeezes ([pressScale]). [large] gives the 56 dp height of a screen's main action.
 *
 * While [loading] the button narrows into a circle as tall as itself (56 dp when [large]) with
 * [PosLoadingIndicator] inside and ignores clicks; it widens back when loading ends. Its layout slot keeps its
 * width, so nothing around it moves.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PosButtonStyle = PosButtonStyle.Filled,
    large: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val motion = PosTheme.motion
    val interactionSource = remember { MutableInteractionSource() }
    val height = if (large) ButtonDefaults.MediumContainerHeight else ButtonDefaults.MinHeight
    val collapse by animateFloatAsState(if (loading) 1f else 0f, motion.spatial(), label = "PosButtonCollapse")
    val labelAlpha by animateFloatAsState(if (loading) 0f else 1f, motion.fastEffects(), label = "PosButtonLabel")
    val indicatorAlpha by animateFloatAsState(if (loading) 1f else 0f, motion.effects(), label = "PosButtonIndicator")
    val baseColors = style.colors()
    // Loading disables the button but keeps its colours, so the circle stays in the style's colour.
    val colors = if (loading && enabled) {
        baseColors.copy(
            disabledContainerColor = baseColors.containerColor,
            disabledContentColor = baseColors.contentColor,
        )
    } else {
        baseColors
    }

    Box(
        modifier = modifier.collapseToCircle({ collapse }, height),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
    ) {
        Button(
            onClick = onClick,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.heightIn(min = height).pressScale(interactionSource),
            enabled = enabled && !loading,
            colors = colors,
            elevation = style.elevation(),
            border = if (style == PosButtonStyle.Outlined) outlinedBorder(enabled) else null,
            contentPadding = contentPadding(style, height, hasIcon = icon != null),
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier.graphicsLayer { alpha = labelAlpha },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)))
                    Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
                }
                Text(text, style = ButtonDefaults.textStyleFor(height), maxLines = 1, softWrap = false)
            }
        }
        if (loading || indicatorAlpha > 0f) {
            PosLoadingIndicator(
                modifier = Modifier
                    .requiredSize(height * LOADING_INDICATOR_FRACTION)
                    .graphicsLayer { alpha = indicatorAlpha },
                color = colors.contentColor,
            )
        }
    }
}

private const val LOADING_INDICATOR_FRACTION = 0.75f

@Composable
private fun PosButtonStyle.colors(): ButtonColors = when (this) {
    PosButtonStyle.Filled -> ButtonDefaults.buttonColors()
    PosButtonStyle.Charge -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
    PosButtonStyle.Tonal -> ButtonDefaults.filledTonalButtonColors()
    // Primary label, as in the approved preview (M3 Expressive defaults to onSurfaceVariant).
    PosButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    PosButtonStyle.Text -> ButtonDefaults.textButtonColors()
}

/** `outline` rather than M3's `outlineVariant`: the edge of a control needs 3:1 against the background. */
@Composable
private fun outlinedBorder(enabled: Boolean): BorderStroke = BorderStroke(
    width = OUTLINE_WIDTH,
    color = if (enabled) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_OUTLINE_ALPHA)
    },
)

private val OUTLINE_WIDTH = 1.dp
private const val DISABLED_OUTLINE_ALPHA = 0.12f

@Composable
private fun PosButtonStyle.elevation(): ButtonElevation? = when (this) {
    PosButtonStyle.Filled, PosButtonStyle.Charge -> ButtonDefaults.buttonElevation()
    PosButtonStyle.Tonal -> ButtonDefaults.filledTonalButtonElevation()
    PosButtonStyle.Outlined, PosButtonStyle.Text -> null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun contentPadding(style: PosButtonStyle, height: Dp, hasIcon: Boolean): PaddingValues = when {
    style == PosButtonStyle.Text && height == ButtonDefaults.MinHeight ->
        if (hasIcon) ButtonDefaults.TextButtonWithIconContentPadding else ButtonDefaults.TextButtonContentPadding
    else -> ButtonDefaults.contentPaddingFor(height, hasStartIcon = hasIcon)
}

/**
 * Narrows the content from its own width to a [diameter]-wide circle as [progress] goes from 0 to 1. The slot keeps
 * the full width and the narrowed content stays centred in it.
 */
private fun Modifier.collapseToCircle(progress: () -> Float, diameter: Dp): Modifier = layout { measurable, constraints ->
    val p = progress()
    if (p == 0f) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val full = if (constraints.hasFixedWidth) {
        constraints.maxWidth
    } else {
        measurable.maxIntrinsicWidth(constraints.maxHeight).coerceIn(constraints.minWidth, constraints.maxWidth)
    }
    val circle = diameter.roundToPx()
    val width = (full + (circle - full) * p).roundToInt().coerceIn(min(full, circle), max(full, circle))
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(full, placeable.height) { placeable.place((full - width) / 2, 0) }
}

@Composable
private fun PosButtonSample() {
    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.m)) {
        PosButton(text = "Charge  Rs 18,750", onClick = {}, style = PosButtonStyle.Charge, large = true)
        PosButton(text = "Log in", onClick = {}, large = true, modifier = Modifier.fillMaxWidth())
        PosButton(text = "Log in", onClick = {}, large = true, loading = true, modifier = Modifier.fillMaxWidth())
        PosButton(text = "Share receipt", onClick = {}, style = PosButtonStyle.Tonal, icon = PosIcons.Share)
        PosButton(text = "Switch shop", onClick = {}, style = PosButtonStyle.Outlined)
        PosButton(text = "Cancel", onClick = {}, style = PosButtonStyle.Text)
        PosButton(text = "Log in", onClick = {}, enabled = false)
    }
}

@Preview
@Composable
private fun PosButtonLightPreview() {
    PosPreview(darkTheme = false) { PosButtonSample() }
}

@Preview
@Composable
private fun PosButtonDarkPreview() {
    PosPreview(darkTheme = true) { PosButtonSample() }
}
