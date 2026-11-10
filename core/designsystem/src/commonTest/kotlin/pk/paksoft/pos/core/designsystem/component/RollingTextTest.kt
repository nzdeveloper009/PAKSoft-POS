package pk.paksoft.pos.core.designsystem.component

import pk.paksoft.pos.core.designsystem.component.RollingSlot.Digit
import pk.paksoft.pos.core.designsystem.component.RollingSlot.Fixed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTextTest {

    @Test
    fun `digits roll and every other character stays fixed`() {
        assertEquals(
            listOf(Fixed('R'), Fixed('s'), Fixed(' '), Digit(1), Digit(8), Fixed(','), Digit(7), Digit(5), Digit(0)),
            rollingSlots("Rs 18,750"),
        )
    }

    @Test
    fun `empty text has no slots`() {
        assertTrue(rollingSlots("").isEmpty())
    }
}
