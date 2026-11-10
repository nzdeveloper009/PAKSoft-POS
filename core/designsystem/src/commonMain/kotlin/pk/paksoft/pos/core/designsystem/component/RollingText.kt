package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.floor
import kotlinx.coroutines.delay
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme

/** One character of a [RollingText]: a digit that rolls, or any other character, which swaps at once. */
internal sealed interface RollingSlot {
    data class Digit(val value: Int) : RollingSlot
    data class Fixed(val char: Char) : RollingSlot
}

/** Splits [text] into slots. Only ASCII digits roll; everything else (currency, separators, spaces) is fixed. */
internal fun rollingSlots(text: String): List<RollingSlot> =
    text.map { char -> if (char in '0'..'9') RollingSlot.Digit(char - '0') else RollingSlot.Fixed(char) }

/**
 * Text whose digits roll vertically like an odometer when it changes (a running total). Slots are matched from the
 * right, so units stay units when the number grows; the right-most digit moves first and each digit to its left
 * follows `rollingStaggerMillis` later. The first value shows without rolling; digits that appear later roll up
 * from 0. With reduced motion it is plain text. Use a style with tabular figures (`PosTheme.typography.money*`).
 */
@Composable
fun RollingText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val motion = PosTheme.motion
    if (motion.reduced) {
        Text(text, style = style, modifier = modifier)
        return
    }
    val mergedStyle = LocalTextStyle.current.merge(style)
    val slots = remember(text) { rollingSlots(text) }
    val shown = remember { ShownOnce() }
    SideEffect { shown.value = true }
    val spoken = AnnotatedString(text)
    Row(modifier = modifier.clearAndSetSemantics { this.text = spoken }) {
        var digitsToTheRight = slots.count { it is RollingSlot.Digit }
        slots.forEachIndexed { index, slot ->
            key(slots.size - index) {
                when (slot) {
                    is RollingSlot.Digit -> {
                        digitsToTheRight--
                        RollingDigit(
                            value = slot.value,
                            style = mergedStyle,
                            delayMillis = digitsToTheRight * motion.rollingStaggerMillis,
                            startValue = if (shown.value) 0 else slot.value,
                        )
                    }
                    is RollingSlot.Fixed -> Text(slot.char.toString(), style = mergedStyle, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

/** Set once the first value is on screen; plain field so setting it causes no recomposition. */
private class ShownOnce {
    var value = false
}

@Composable
private fun RollingDigit(value: Int, style: TextStyle, delayMillis: Int, startValue: Int) {
    val motion = PosTheme.motion
    val position = remember { Animatable(startValue.toFloat()) }
    LaunchedEffect(value) {
        if (position.value == value.toFloat()) return@LaunchedEffect
        delay(delayMillis.toLong())
        position.animateTo(value.toFloat(), motion.spatial())
    }
    val measurer = rememberTextMeasurer(cacheSize = DIGITS)
    val digits = remember(style, measurer) { List(DIGITS) { measurer.measure(it.toString(), style, maxLines = 1) } }
    val cellWidth = digits.maxOf { it.size.width }
    val cellHeight = digits.maxOf { it.size.height }
    val color = style.color.takeOrElse { LocalContentColor.current }
    val density = LocalDensity.current
    Spacer(
        modifier = Modifier
            .size(with(density) { cellWidth.toDp() }, with(density) { cellHeight.toDp() })
            .clipToBounds()
            .drawBehind {
                val p = position.value.coerceIn(0f, (DIGITS - 1).toFloat())
                val lower = floor(p).toInt()
                val fraction = p - lower
                val top = digits[lower]
                drawText(top, color, Offset((cellWidth - top.size.width) / 2f, -fraction * cellHeight))
                if (fraction > 0f) {
                    val next = digits[lower + 1]
                    drawText(next, color, Offset((cellWidth - next.size.width) / 2f, (1f - fraction) * cellHeight))
                }
            },
    )
}

private const val DIGITS = 10

@Composable
private fun RollingTextSample() {
    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.s)) {
        RollingText("Rs 18,750", style = PosTheme.typography.moneyLarge)
        RollingText("Rs 1,200", style = PosTheme.typography.moneySmall)
    }
}

@Preview
@Composable
private fun RollingTextLightPreview() {
    PosPreview(darkTheme = false) { RollingTextSample() }
}

@Preview
@Composable
private fun RollingTextDarkPreview() {
    PosPreview(darkTheme = true) { RollingTextSample() }
}
