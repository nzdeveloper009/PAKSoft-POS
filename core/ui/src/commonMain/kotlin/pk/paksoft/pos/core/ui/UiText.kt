package pk.paksoft.pos.core.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Text a ViewModel can hand to the UI without touching Android or iOS resources. */
sealed interface UiText {
    data class Raw(val text: String) : UiText
    data class Res(val res: StringResource, val args: List<Any> = emptyList()) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Raw -> text
    is UiText.Res -> stringResource(res, *args.toTypedArray())
}
