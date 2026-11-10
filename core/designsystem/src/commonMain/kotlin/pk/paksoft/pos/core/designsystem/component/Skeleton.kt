package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * While [visible], draws a placeholder block (`surfaceContainerHigh`, small corners) instead of the content, with a
 * highlight sweeping across it. With reduced motion the block is static. Clip first for another shape.
 */
fun Modifier.skeleton(visible: Boolean): Modifier = if (!visible) this else composed {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceBright
    val shape = MaterialTheme.shapes.extraSmall
    val motion = PosTheme.motion
    val sweep = if (motion.reduced) {
        null
    } else {
        rememberInfiniteTransition(label = "skeleton").animateFloat(0f, 1f, motion.shimmer(), label = "sweep")
    }
    val clear = highlight.copy(alpha = 0f)
    drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawWithContent {
            drawOutline(outline, base)
            val s = sweep?.value ?: return@onDrawWithContent
            // The highlight band is as wide as the block and travels from fully left of it to fully right.
            val start = -size.width + s * 2f * size.width
            val brush = Brush.linearGradient(
                colors = listOf(clear, highlight, clear),
                start = Offset(start, 0f),
                end = Offset(start + size.width, 0f),
            )
            drawOutline(outline, brush)
        }
    }
}

/** A standalone placeholder block; size it with [modifier]. */
@Composable
fun SkeletonBox(modifier: Modifier) {
    Box(modifier.skeleton(visible = true))
}

@Composable
private fun SkeletonSample() {
    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.l)) {
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(PosSpacing.m), verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.size(40.dp))
                Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.s)) {
                    SkeletonBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.35f).height(12.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SkeletonLightPreview() {
    PosPreview(darkTheme = false) { SkeletonSample() }
}

@Preview
@Composable
private fun SkeletonDarkPreview() {
    PosPreview(darkTheme = true) { SkeletonSample() }
}
