package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * A filter chip (period, category, quick amount). Selected: secondaryContainer with a check mark that grows in
 * (`fastSpatial`); unselected: outlined. Ripple and press squeeze like every tappable surface.
 */
@Composable
fun PosFilterChip(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val motion = PosTheme.motion
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val shape = MaterialTheme.shapes.small
    val container by animateColorAsState(
        if (selected) colors.secondaryContainer else colors.secondaryContainer.copy(alpha = 0f),
        motion.effects(),
        label = "PosFilterChipContainer",
    )
    val content by animateColorAsState(
        if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        motion.effects(),
        label = "PosFilterChipContent",
    )
    val outline by animateColorAsState(
        if (selected) colors.outlineVariant.copy(alpha = 0f) else colors.outlineVariant,
        motion.effects(),
        label = "PosFilterChipOutline",
    )
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .pressScale(interactionSource)
            .heightIn(min = CHIP_HEIGHT)
            .clip(shape)
            .background(container)
            .border(BorderStroke(1.dp, outline), shape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Checkbox,
                onClick = onClick,
            )
            .padding(horizontal = CHIP_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(motion.fastSpatial()) + scaleIn(motion.fastSpatial()),
            exit = shrinkHorizontally(motion.fastSpatial()) + scaleOut(motion.fastSpatial()),
        ) {
            Icon(
                PosIcons.Check,
                contentDescription = null,
                tint = content,
                modifier = Modifier.padding(end = CHECK_GAP).size(CHECK_SIZE),
            )
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
    }
}

private val CHIP_HEIGHT = 36.dp
private val CHIP_PADDING = 14.dp
private val CHECK_SIZE = 18.dp
private val CHECK_GAP = 6.dp

@Composable
private fun PosFilterChipSample() {
    var selected by remember { mutableIntStateOf(1) }
    Row(horizontalArrangement = Arrangement.spacedBy(PosSpacing.s)) {
        listOf("Today", "This month", "This year").forEachIndexed { index, label ->
            PosFilterChip(selected = selected == index, onClick = { selected = index }, label = label)
        }
    }
}

@Preview
@Composable
private fun PosFilterChipLightPreview() {
    PosPreview(darkTheme = false) { PosFilterChipSample() }
}

@Preview
@Composable
private fun PosFilterChipDarkPreview() {
    PosPreview(darkTheme = true) { PosFilterChipSample() }
}
