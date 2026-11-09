package pk.paksoft.pos.app

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.testing.FakeSessionRepository
import pk.paksoft.pos.core.testing.TestData

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun `starts the session once and mirrors its state`() = runTest {
        val session = FakeSessionRepository(initial = SessionState.Starting)

        val vm = AppViewModel(session)

        assertEquals(1, session.startCalls)
        assertEquals(SessionState.Starting, vm.state.value)

        session.state.value = SessionState.NoShop
        assertEquals(SessionState.NoShop, vm.state.value)

        val loggedIn = SessionState.LoggedIn(TestData.smf, TestData.userSession())
        session.state.value = loggedIn
        assertEquals(loggedIn, vm.state.value)
        assertEquals(1, session.startCalls)
    }
}
