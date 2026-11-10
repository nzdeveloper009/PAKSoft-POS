package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PosMotionTest {

    private fun assertSpring(spec: Any, damping: Float, stiffness: Float) {
        val s = assertIs<SpringSpec<*>>(spec)
        assertEquals(damping, s.dampingRatio)
        assertEquals(stiffness, s.stiffness)
    }

    @Test
    fun `spring tokens match the spec table`() {
        val m = PosMotion(reduced = false)
        assertSpring(m.fastSpatial<Float>(), 0.6f, 800f)
        assertSpring(m.spatial<Float>(), 0.8f, 380f)
        assertSpring(m.slowSpatial<Float>(), 0.8f, 200f)
        assertSpring(m.fastEffects<Float>(), 1.0f, 3800f)
        assertSpring(m.effects<Float>(), 1.0f, 1600f)
        assertSpring(m.slowEffects<Float>(), 1.0f, 800f)
        assertSpring(m.bouncy<Float>(), 0.45f, 500f)
        assertSpring(m.countUp<Float>(), 1.0f, 120f)
    }

    @Test
    fun `reduced motion makes spatial specs snap`() {
        val m = PosMotion(reduced = true)
        assertIs<SnapSpec<*>>(m.spatial<Float>())
        assertIs<SnapSpec<*>>(m.fastSpatial<Float>())
        assertIs<SnapSpec<*>>(m.slowSpatial<Float>())
        assertIs<SnapSpec<*>>(m.bouncy<Float>())
    }

    @Test
    fun `reduced motion makes effects a short tween`() {
        val m = PosMotion(reduced = true)
        assertEquals(120, assertIs<TweenSpec<*>>(m.effects<Float>()).durationMillis)
        assertEquals(120, assertIs<TweenSpec<*>>(m.fastEffects<Float>()).durationMillis)
        assertEquals(120, assertIs<TweenSpec<*>>(m.slowEffects<Float>()).durationMillis)
    }

    @Test
    fun `stagger and enter offset are fixed`() {
        val m = PosMotion(reduced = false)
        assertEquals(50, m.staggerStepMillis)
        assertEquals(18f, m.enterOffset.value)
    }
}
