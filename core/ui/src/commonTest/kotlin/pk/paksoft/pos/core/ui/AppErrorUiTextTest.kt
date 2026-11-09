package pk.paksoft.pos.core.ui

import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.ui.resources.Res
import pk.paksoft.pos.core.ui.resources.error_offline
import pk.paksoft.pos.core.ui.resources.error_server
import pk.paksoft.pos.core.ui.resources.error_shop_not_connected
import pk.paksoft.pos.core.ui.resources.error_timeout
import pk.paksoft.pos.core.ui.resources.error_unexpected
import pk.paksoft.pos.core.ui.resources.session_expired
import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorUiTextTest {
    @Test fun `server 404 without message means shop not connected`() {
        assertEquals(UiText.Res(Res.string.error_shop_not_connected), AppError.Server(404, null).toUiText())
    }

    @Test fun `server message is shown as given`() { assertEquals(UiText.Raw("Down"), AppError.Server(503, "Down").toUiText()) }

    @Test fun `server without message or 404 is a generic server error`() {
        assertEquals(UiText.Res(Res.string.error_server), AppError.Server(500, null).toUiText())
    }

    @Test fun `business errors show the server message`() {
        assertEquals(
            UiText.Raw("The username/email or password is incorrect."),
            AppError.Business("invalid_credentials", "The username/email or password is incorrect.", null).toUiText(),
        )
    }

    @Test fun `offline timeout unauthenticated unexpected map to their strings`() {
        assertEquals(UiText.Res(Res.string.error_offline), AppError.Offline.toUiText())
        assertEquals(UiText.Res(Res.string.error_timeout), AppError.Timeout.toUiText())
        assertEquals(UiText.Res(Res.string.session_expired), AppError.Unauthenticated.toUiText())
        assertEquals(UiText.Res(Res.string.error_unexpected), AppError.Unexpected(null).toUiText())
    }
}
