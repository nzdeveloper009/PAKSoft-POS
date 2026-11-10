package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class PosMotionSchemeTest {

    @Test
    fun `reduced motion scheme snaps every spatial spec`() {
        val scheme = posMotionScheme(PosMotion(reduced = true))
        assertIs<SnapSpec<*>>(scheme.defaultSpatialSpec<Float>())
        assertIs<SnapSpec<*>>(scheme.fastSpatialSpec<Float>())
        assertIs<SnapSpec<*>>(scheme.slowSpatialSpec<Float>())
    }

    @Test
    fun `reduced motion scheme makes every effects spec a 120 ms tween`() {
        val scheme = posMotionScheme(PosMotion(reduced = true))
        assertEquals(120, assertIs<TweenSpec<*>>(scheme.defaultEffectsSpec<Float>()).durationMillis)
        assertEquals(120, assertIs<TweenSpec<*>>(scheme.fastEffectsSpec<Float>()).durationMillis)
        assertEquals(120, assertIs<TweenSpec<*>>(scheme.slowEffectsSpec<Float>()).durationMillis)
    }

    @Test
    fun `full motion keeps the expressive springs`() {
        val spatial = assertIs<SpringSpec<*>>(posMotionScheme(PosMotion(reduced = false)).defaultSpatialSpec<Float>())
        assertEquals(0.8f, spatial.dampingRatio)
        assertEquals(380f, spatial.stiffness)
    }
}
