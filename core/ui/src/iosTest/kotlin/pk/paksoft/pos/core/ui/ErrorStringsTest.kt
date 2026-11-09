package pk.paksoft.pos.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import pk.paksoft.pos.core.ui.resources.Res
import pk.paksoft.pos.core.ui.resources.error_shop_not_connected

/** iOS only: Android host tests can't load Compose resources (android.content.res.Resources isn't mocked). */
class ErrorStringsTest {
    // Compose resources don't unescape Android-style \' in strings.xml; the apostrophe must be written plainly.
    @Test fun `shop not connected text has a plain apostrophe`() = runTest {
        assertEquals("This shop isn't connected to the app yet.", getString(Res.string.error_shop_not_connected))
    }
}
