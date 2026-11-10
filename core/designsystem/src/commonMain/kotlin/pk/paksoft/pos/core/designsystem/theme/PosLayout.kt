package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Widest a screen's content grows on a wide window (tablet, landscape); it centres in the window beyond that. Use
 * with `Modifier.widthIn(max = …).fillMaxWidth()`.
 */
object PosLayout {
    /** A form or a short list of choices: the shop picker, the login form. */
    val formMaxWidth = 420.dp

    /** A screen of cards and actions: More. */
    val contentMaxWidth = 560.dp
}
