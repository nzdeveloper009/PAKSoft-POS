package pk.paksoft.pos.core.designsystem.motion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PredictiveBackCornersTest {

    @Test
    fun `a gesture pulling the leaving screen rounds it`() {
        assertTrue(predictiveBackPulled(pulled = false, inGesture = true, leaving = true))
    }

    @Test
    fun `a completed gesture keeps the corners while the screen finishes leaving`() {
        assertTrue(predictiveBackPulled(pulled = true, inGesture = false, leaving = true))
    }

    @Test
    fun `an ordinary pop or push or tab switch never rounds`() {
        assertFalse(predictiveBackPulled(pulled = false, inGesture = false, leaving = true))
    }

    @Test
    fun `a screen that stays after a cancelled gesture or that a gesture reveals is square`() {
        assertFalse(predictiveBackPulled(pulled = true, inGesture = false, leaving = false))
        assertFalse(predictiveBackPulled(pulled = false, inGesture = true, leaving = false))
    }
}
