package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class CountUpTest {

    @Test
    fun `count starts at from and ends at to`() {
        assertEquals(0L, countAt(0, 482350, 0f))
        assertEquals(482350L, countAt(0, 482350, 1f))
    }

    @Test
    fun `count goes down as well as up`() {
        assertEquals(75L, countAt(100, 50, 0.5f))
    }

    @Test
    fun `overshoot passes through unclamped`() {
        assertEquals(11L, countAt(0, 10, 1.08f))
    }
}
