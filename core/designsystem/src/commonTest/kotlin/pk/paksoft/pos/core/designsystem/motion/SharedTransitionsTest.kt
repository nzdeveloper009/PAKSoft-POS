package pk.paksoft.pos.core.designsystem.motion

import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SharedTransitionsTest {

    @Test
    fun `shared transitions run with both scopes and full motion`() {
        assertTrue(sharedTransitionEnabled(hasSharedScope = true, hasVisibilityScope = true, reduced = false))
    }

    @Test
    fun `a missing shared scope turns shared transitions off`() {
        assertFalse(sharedTransitionEnabled(hasSharedScope = false, hasVisibilityScope = true, reduced = false))
    }

    @Test
    fun `a missing visibility scope turns shared transitions off`() {
        assertFalse(sharedTransitionEnabled(hasSharedScope = true, hasVisibilityScope = false, reduced = false))
    }

    @Test
    fun `reduced motion turns shared transitions off`() {
        assertFalse(sharedTransitionEnabled(hasSharedScope = true, hasVisibilityScope = true, reduced = true))
    }

    @Test
    fun `no scopes at all as in previews turns shared transitions off`() {
        assertFalse(sharedTransitionEnabled(hasSharedScope = false, hasVisibilityScope = false, reduced = false))
    }

    @Test
    fun `shop keys are distinct per part and per shop`() {
        assertEquals("shop-card-smf", shopCardKey("smf"))
        assertEquals("shop-avatar-smf", shopAvatarKey("smf"))
        assertEquals("shop-name-smf", shopNameKey("smf"))
        assertEquals(3, setOf(shopCardKey("bsf"), shopAvatarKey("bsf"), shopNameKey("bsf")).size)
    }

    @Test
    fun `a remeasured container lays its content out again at every size`() {
        assertEquals(RemeasureToBounds, PosSharedResize.Remeasure.resizeMode())
    }

    @Test
    fun `scaled content keeps its final layout and is not remeasured`() {
        assertNotEquals(RemeasureToBounds, PosSharedResize.Scale.resizeMode())
    }
}
