package pk.paksoft.pos.app

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
