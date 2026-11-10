package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/**
 * The app's own mark, for screens that belong to no shop yet (the shop picker): a small four-sided cookie and
 * [name] in primary, heavy and lightly tracked. Shops wear their own marks ([ShopAvatar]) everywhere else.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosBrandMark(name: String, modifier: Modifier = Modifier) {
    val shape = MaterialShapes.Cookie4Sided.toShape()
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PosSpacing.s),
    ) {
        Box(Modifier.size(MARK_SIZE).background(color, shape))
        Text(
            text = name,
            color = color,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = TRACKING),
            maxLines = 1,
        )
    }
}

private val MARK_SIZE = 28.dp
private val TRACKING = 0.06.em

@Preview
@Composable
private fun PosBrandMarkLightPreview() {
    PosPreview(darkTheme = false) { PosBrandMark("PAKSOFT POS") }
}

@Preview
@Composable
private fun PosBrandMarkDarkPreview() {
    PosPreview(darkTheme = true) { PosBrandMark("PAKSOFT POS") }
}
