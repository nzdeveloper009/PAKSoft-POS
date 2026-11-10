package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class EnterStaggerTest {

    @Test
    fun `each index waits one more step`() {
        assertEquals(150, staggerDelayMillis(3, 50, reduced = false))
    }

    @Test
    fun `index is clamped at 8`() {
        assertEquals(400, staggerDelayMillis(20, 50, false))
    }

    @Test
    fun `reduced motion never waits`() {
        assertEquals(0, staggerDelayMillis(3, 50, reduced = true))
    }
}
