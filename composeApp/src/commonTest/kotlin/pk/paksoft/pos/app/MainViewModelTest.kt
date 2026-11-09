package pk.paksoft.pos.app

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pk.paksoft.pos.app.main.MainAction
import pk.paksoft.pos.app.main.MainViewModel
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.testing.FakeBootstrapRepository
import pk.paksoft.pos.core.testing.FakeSessionRepository
import pk.paksoft.pos.core.testing.TestData
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.resources.Res as UiRes
import pk.paksoft.pos.core.ui.resources.error_offline

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val loggedIn = SessionState.LoggedIn(TestData.smf, TestData.userSession())
    private val loggedOut = SessionState.LoggedOut(TestData.smf, null)
    private val session = FakeSessionRepository(initial = loggedIn)
    private val bootstrap = FakeBootstrapRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun `refreshes each time the user logs in`() = runTest {
        MainViewModel(bootstrap, session)
        assertEquals(1, bootstrap.refreshCalls)

        session.state.value = loggedOut
        session.state.value = loggedIn
        assertEquals(2, bootstrap.refreshCalls)

        // A /me answer that only updates the signed-in user is not a new login.
        session.state.value = loggedIn.copy(session = TestData.userSession(name = "cashier2"))
        assertEquals(2, bootstrap.refreshCalls)
    }

    @Test fun `refresh failure shows a banner that the next login clears`() = runTest {
        bootstrap.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = MainViewModel(bootstrap, session)
        assertEquals(UiText.Res(UiRes.string.error_offline), vm.state.value.banner)

        bootstrap.refreshResult = AppResult.Success(Unit)
        session.state.value = loggedOut
        session.state.value = loggedIn
        assertNull(vm.state.value.banner)
        assertEquals(2, bootstrap.refreshCalls)
    }

    @Test fun `unauthenticated refresh shows no banner`() = runTest {
        bootstrap.refreshResult = AppResult.Failure(AppError.Unauthenticated)
        val vm = MainViewModel(bootstrap, session)
        assertEquals(1, bootstrap.refreshCalls)
        assertNull(vm.state.value.banner)
    }

    @Test fun `retry refreshes again and clears the banner on success`() = runTest {
        bootstrap.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = MainViewModel(bootstrap, session)

        bootstrap.refreshResult = AppResult.Success(Unit)
        vm.onAction(MainAction.RetryRefresh)

        assertEquals(2, bootstrap.refreshCalls)
        assertNull(vm.state.value.banner)
    }

    @Test fun `mirrors cached shop settings`() = runTest {
        val vm = MainViewModel(bootstrap, session)
        assertNull(vm.state.value.settings)

        bootstrap.settingsFlow.value = TestData.shopSettings(ledger = false)
        assertEquals(TestData.shopSettings(ledger = false), vm.state.value.settings)
    }
}
