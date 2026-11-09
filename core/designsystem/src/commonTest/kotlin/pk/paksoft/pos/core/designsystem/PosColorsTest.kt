package pk.paksoft.pos.core.designsystem

import androidx.compose.ui.graphics.Color
import pk.paksoft.pos.core.designsystem.theme.PosLightColorScheme
import pk.paksoft.pos.core.designsystem.theme.PosLightExtendedColors
import kotlin.test.Test
import kotlin.test.assertEquals

class PosColorsTest {
    @Test fun `brand primary matches the web`() { assertEquals(Color(0xFF7C5CC4), PosLightColorScheme.primary) }

    @Test fun `payment colors match pos blade`() {
        val c = PosLightExtendedColors
        assertEquals(Color(0xFF0984E3), c.payCard); assertEquals(Color(0xFF00CEC9), c.payCash); assertEquals(Color(0xFF213170), c.payPaypal)
        assertEquals(Color(0xFFE28D02), c.payDraft); assertEquals(Color(0xFFFD7272), c.payCheque); assertEquals(Color(0xFF5F27CD), c.payGiftCard)
        assertEquals(Color(0xFFB33771), c.payDeposit); assertEquals(Color(0xFFD63031), c.payCancel); assertEquals(Color(0xFFFFC107), c.payRecent)
    }
}
