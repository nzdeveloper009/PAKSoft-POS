package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `previews and reduced motion show sections without an entrance`() {
        assertTrue(enterStaggerSkipped(reduced = false, inspectionMode = true))
        assertTrue(enterStaggerSkipped(reduced = true, inspectionMode = false))
        assertFalse(enterStaggerSkipped(reduced = false, inspectionMode = false))
    }
}
