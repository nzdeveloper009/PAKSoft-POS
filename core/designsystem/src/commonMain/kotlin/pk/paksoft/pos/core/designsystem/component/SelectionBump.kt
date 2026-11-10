package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * A small spring bump (1 → 1.1 → 1, `fastSpatial`) each time [selected] turns true, e.g. a navigation bar icon.
 * Nothing on first show, while staying selected, or with reduced motion. A bump cut short by a quick deselect still
 * settles back to 1.
 */
@Composable
fun Modifier.selectionBump(selected: Boolean): Modifier {
    val motion = PosTheme.motion
    val scale = remember { Animatable(1f) }
    // Only read inside the effect, so updating it never recomposes.
    var lastSelected by remember { mutableStateOf(selected) }
    LaunchedEffect(selected) {
        val bump = selectionBumps(lastSelected, selected, motion.reduced)
        lastSelected = selected
        if (bump) scale.animateTo(BUMP_SCALE, motion.fastSpatial())
        scale.animateTo(1f, motion.fastSpatial())
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/** Only the change to selected bumps; reduced motion never does. */
internal fun selectionBumps(wasSelected: Boolean, selected: Boolean, reduced: Boolean): Boolean =
    selected && !wasSelected && !reduced

private const val BUMP_SCALE = 1.1f
