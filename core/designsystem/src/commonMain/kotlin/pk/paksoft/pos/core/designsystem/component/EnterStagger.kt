package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * Entrance for the [index]-th section of a screen: on first composition it rises `enterOffset` and fades in with
 * the `spatial` spring, `staggerStepMillis` after the section before it (indexes past 8 wait as long as 8).
 * With reduced motion the section is simply there.
 */
@Composable
fun Modifier.enterStagger(index: Int): Modifier {
    val motion = PosTheme.motion
    if (motion.reduced) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(staggerDelayMillis(index, motion.staggerStepMillis, reduced = false).toLong())
        progress.animateTo(1f, motion.spatial())
    }
    val offsetPx = with(LocalDensity.current) { motion.enterOffset.toPx() }
    return graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * offsetPx
    }
}

/** How long section [index] waits before entering: one [stepMillis] per index, at most 8 steps, none when [reduced]. */
internal fun staggerDelayMillis(index: Int, stepMillis: Int, reduced: Boolean): Int =
    if (reduced) 0 else index.coerceIn(0, MAX_STAGGER_INDEX) * stepMillis

private const val MAX_STAGGER_INDEX = 8

@Composable
private fun EnterStaggerSample() {
    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.m)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .enterStagger(index)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(PosSpacing.l),
            ) {
                Text("Section ${index + 1}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview
@Composable
private fun EnterStaggerLightPreview() {
    PosPreview(darkTheme = false) { EnterStaggerSample() }
}

@Preview
@Composable
private fun EnterStaggerDarkPreview() {
    PosPreview(darkTheme = true) { EnterStaggerSample() }
}
