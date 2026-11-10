package pk.paksoft.pos.core.designsystem.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import pk.paksoft.pos.core.designsystem.theme.PosMotion

class PosTransitionsTest {

    @Test
    fun `the outgoing screen slides on the spatial spring`() {
        val spring = assertIs<SpringSpec<*>>(PosTransitions.outgoingSlideSpec(PosMotion(reduced = false)))
        assertEquals(0.8f, spring.dampingRatio)
        assertEquals(380f, spring.stiffness)
    }

    @Test
    fun `with reduced motion the outgoing screen does not slide`() {
        assertIs<SnapSpec<*>>(PosTransitions.outgoingSlideSpec(PosMotion(reduced = true)))
    }

    @Test
    fun `the outgoing screen fades with fastEffects`() {
        assertIs<SpringSpec<*>>(PosTransitions.outgoingFadeSpec(PosMotion(reduced = false)))
        assertEquals(120, assertIs<TweenSpec<*>>(PosTransitions.outgoingFadeSpec(PosMotion(reduced = true))).durationMillis)
    }
}
