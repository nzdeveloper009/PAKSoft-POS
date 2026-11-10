package pk.paksoft.pos.app

import androidx.compose.animation.EnterExitState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import pk.paksoft.pos.core.model.SessionNotice
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.testing.TestData

class SessionTransitionsTest {

    @Test
    fun `each session state maps to its screen`() {
        assertEquals(SessionScreen.Starting, sessionScreen(SessionState.Starting))
        assertEquals(SessionScreen.ShopPicker, sessionScreen(SessionState.NoShop))
        assertEquals(SessionScreen.Login, sessionScreen(SessionState.LoggedOut(TestData.smf, notice = null)))
        assertEquals(SessionScreen.Home, sessionScreen(SessionState.LoggedIn(TestData.smf, TestData.userSession())))
    }

    @Test
    fun `picking a shop moves forward to its login`() {
        assertEquals(SessionTransition.SharedForward, sessionTransition(SessionScreen.ShopPicker, SessionScreen.Login))
    }

    @Test
    fun `changing shop from the login goes back to the picker`() {
        assertEquals(SessionTransition.SharedBack, sessionTransition(SessionScreen.Login, SessionScreen.ShopPicker))
    }

    @Test
    fun `logging in fades through to home`() {
        assertEquals(SessionTransition.FadeThrough, sessionTransition(SessionScreen.Login, SessionScreen.Home))
    }

    @Test
    fun `logging out goes back to the login`() {
        assertEquals(SessionTransition.SharedBack, sessionTransition(SessionScreen.Home, SessionScreen.Login))
    }

    @Test
    fun `switching shop from home goes back to the picker`() {
        assertEquals(SessionTransition.SharedBack, sessionTransition(SessionScreen.Home, SessionScreen.ShopPicker))
    }

    @Test
    fun `leaving the starting screen always fades through`() {
        SessionScreen.entries.forEach { to ->
            assertEquals(SessionTransition.FadeThrough, sessionTransition(SessionScreen.Starting, to), "Starting -> $to")
        }
    }

    @Test
    fun `content key is a plain string so saved state survives process death`() {
        // A Pair or an enum inside one hashes by identity, which changes every process; a String hashes by content.
        assertEquals("Starting", sessionContentKey(SessionState.Starting))
        assertEquals("ShopPicker", sessionContentKey(SessionState.NoShop))
        assertEquals("Login:smf", sessionContentKey(SessionState.LoggedOut(TestData.smf, notice = null)))
        assertEquals("Home", sessionContentKey(SessionState.LoggedIn(TestData.smf, TestData.userSession())))
    }

    @Test
    fun `a refreshed session or a notice keeps the content key`() {
        assertEquals(
            sessionContentKey(SessionState.LoggedIn(TestData.smf, TestData.userSession(name = "a"))),
            sessionContentKey(SessionState.LoggedIn(TestData.smf, TestData.userSession(name = "b"))),
        )
        assertEquals(
            sessionContentKey(SessionState.LoggedOut(TestData.smf, notice = null)),
            sessionContentKey(SessionState.LoggedOut(TestData.smf, SessionNotice.SessionExpired)),
        )
    }

    @Test
    fun `another shop gets its own login content key`() {
        assertNotEquals(
            sessionContentKey(SessionState.LoggedOut(TestData.smf, notice = null)),
            sessionContentKey(SessionState.LoggedOut(TestData.local, notice = null)),
        )
    }

    @Test
    fun `a screen animating out takes no input`() {
        // AnimatedContent targets PostExit for the leaving screen: a second tap there must not act again.
        assertFalse(sessionScreenTakesInput(EnterExitState.PostExit))
    }

    @Test
    fun `the screen coming in or staying takes input`() {
        // Entering content targets Visible from its first frame; an interrupted exit retargets Visible too.
        assertTrue(sessionScreenTakesInput(EnterExitState.Visible))
    }
}
