package pk.paksoft.pos.core.ui

import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.ui.resources.Res
import pk.paksoft.pos.core.ui.resources.error_offline
import pk.paksoft.pos.core.ui.resources.error_server
import pk.paksoft.pos.core.ui.resources.error_shop_not_connected
import pk.paksoft.pos.core.ui.resources.error_timeout
import pk.paksoft.pos.core.ui.resources.error_unexpected
import pk.paksoft.pos.core.ui.resources.session_expired

/** Server-provided messages are shown as given; everything else maps to a string resource. */
fun AppError.toUiText(): UiText = when (this) {
    AppError.Offline -> UiText.Res(Res.string.error_offline)
    AppError.Timeout -> UiText.Res(Res.string.error_timeout)
    AppError.Unauthenticated -> UiText.Res(Res.string.session_expired)
    is AppError.Forbidden -> UiText.Raw(message)
    is AppError.NotFound -> UiText.Raw(message)
    is AppError.Validation -> UiText.Raw(message)
    is AppError.Conflict -> UiText.Raw(message)
    is AppError.Business -> UiText.Raw(message)
    is AppError.Server -> {
        val serverMessage = message
        when {
            serverMessage != null -> UiText.Raw(serverMessage)
            status == 404 -> UiText.Res(Res.string.error_shop_not_connected)
            else -> UiText.Res(Res.string.error_server)
        }
    }
    is AppError.Unexpected -> UiText.Res(Res.string.error_unexpected)
}
