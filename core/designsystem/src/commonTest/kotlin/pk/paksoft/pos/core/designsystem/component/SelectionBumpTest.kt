package pk.paksoft.pos.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionBumpTest {

    @Test
    fun `becoming selected bumps`() {
        assertTrue(selectionBumps(wasSelected = false, selected = true, reduced = false))
    }

    @Test
    fun `staying selected or being deselected does not bump`() {
        assertFalse(selectionBumps(wasSelected = true, selected = true, reduced = false))
        assertFalse(selectionBumps(wasSelected = true, selected = false, reduced = false))
        assertFalse(selectionBumps(wasSelected = false, selected = false, reduced = false))
    }

    @Test
    fun `reduced motion never bumps`() {
        assertFalse(selectionBumps(wasSelected = false, selected = true, reduced = true))
    }
}
