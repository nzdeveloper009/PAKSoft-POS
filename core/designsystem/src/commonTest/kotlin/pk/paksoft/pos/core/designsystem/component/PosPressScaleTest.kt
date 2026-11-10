package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class PosPressScaleTest {

    @Test
    fun `press squeezes match the spec`() {
        // Spec "Press feel": large cards 0.97, every other tappable surface 0.95, icon buttons 0.88.
        assertEquals(0.97f, PosPressScale.card)
        assertEquals(0.95f, PosPressScale.control)
        assertEquals(0.88f, PosPressScale.icon)
    }

    @Test
    fun `smaller targets squeeze more`() {
        assertEquals(true, PosPressScale.icon < PosPressScale.control && PosPressScale.control < PosPressScale.card)
    }
}
