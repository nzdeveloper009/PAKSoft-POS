package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToLong
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/**
 * A whole number that counts to [target] with the `countUp` spring: from 0 the first time, then from wherever it
 * is when [target] changes. With reduced motion it jumps straight to [target]. Format the value yourself
 * (money is paisa: count the paisa, format each frame).
 */
@Composable
fun animateCountAsState(target: Long): State<Long> {
    val motion = PosTheme.motion
    val count = remember { mutableLongStateOf(if (motion.reduced) target else 0L) }
    val fraction = remember { Animatable(0f) }
    LaunchedEffect(target, motion.reduced) {
        val from = count.longValue
        if (motion.reduced || from == target) {
            count.longValue = target
            return@LaunchedEffect
        }
        fraction.snapTo(0f)
        fraction.animateTo(1f, motion.countUp()) { count.longValue = countAt(from, target, value) }
        count.longValue = target
    }
    return count
}

/** The count [fraction] of the way from [from] to [to], rounded to the nearest whole number. Not clamped. */
internal fun countAt(from: Long, to: Long, fraction: Float): Long =
    from + ((to - from).toDouble() * fraction).roundToLong()

@Composable
private fun CountUpSample() {
    val count by animateCountAsState(482_350)
    Text(count.toString(), style = PosTheme.typography.moneyLarge)
}

@Preview
@Composable
private fun CountUpLightPreview() {
    PosPreview(darkTheme = false) { CountUpSample() }
}

@Preview
@Composable
private fun CountUpDarkPreview() {
    PosPreview(darkTheme = true) { CountUpSample() }
}
