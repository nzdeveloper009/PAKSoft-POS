package pk.paksoft.pos.feature.auth

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.model.SessionNotice
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.testing.FakeSessionRepository
import pk.paksoft.pos.core.testing.TestData
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.resources.error_offline
import pk.paksoft.pos.core.ui.resources.error_shop_not_connected
import pk.paksoft.pos.core.ui.resources.session_expired
import pk.paksoft.pos.feature.auth.login.LoginAction.ChangeShop
import pk.paksoft.pos.feature.auth.login.LoginAction.LoginChanged
import pk.paksoft.pos.feature.auth.login.LoginAction.PasswordChanged
import pk.paksoft.pos.feature.auth.login.LoginAction.Submit
import pk.paksoft.pos.feature.auth.login.LoginViewModel
import pk.paksoft.pos.feature.auth.resources.login_error_login_required
import pk.paksoft.pos.feature.auth.resources.login_error_password_required
import pk.paksoft.pos.core.ui.resources.Res as UiRes
import pk.paksoft.pos.feature.auth.resources.Res as AuthRes

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var fake: FakeSessionRepository

    @BeforeTest fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fake = FakeSessionRepository(initial = SessionState.LoggedOut(TestData.smf, null))
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun newVm() = LoginViewModel(fake)

    private fun LoginViewModel.fill() {
        onAction(LoginChanged("cashier1"))
        onAction(PasswordChanged("pw"))
    }

    @Test fun `shows the shop name`() = runTest {
        assertEquals("Super Madina Solar Fan", newVm().state.value.shopName)
    }

    @Test fun `shows the shop id and host of the logged out shop`() = runTest {
        val state = newVm().state.value
        assertEquals("smf", state.shopId)
        assertEquals("pbsf.org.pk/smf", state.shopHost)
    }

    @Test fun `demo shop shows the demo hint`() = runTest {
        assertEquals(false, newVm().state.value.isDemoShop)
        fake = FakeSessionRepository(initial = SessionState.LoggedOut(Shops.demo, null))
        assertEquals(true, newVm().state.value.isDemoShop)
    }

    @Test fun `blank fields show errors and do not call login`() = runTest {
        val vm = newVm()
        vm.onAction(Submit)
        assertEquals(UiText.Res(AuthRes.string.login_error_login_required), vm.state.value.loginError)
        assertEquals(UiText.Res(AuthRes.string.login_error_password_required), vm.state.value.passwordError)
        assertTrue(fake.loginCalls.isEmpty())
    }

    @Test fun `second submit while submitting is ignored`() = runTest {
        val vm = newVm()
        fake.loginGate = CompletableDeferred()
        vm.fill()
        vm.onAction(Submit)
        vm.onAction(Submit)
        assertTrue(vm.state.value.isSubmitting)
        fake.loginGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, fake.loginCalls.size)
    }

    @Test fun `validation errors land on their fields`() = runTest {
        val vm = newVm()
        fake.loginResult = AppResult.Failure(
            AppError.Validation("Please check the form.", mapOf("login" to listOf("Login is required."))),
        )
        vm.fill()
        vm.onAction(Submit)
        assertEquals(UiText.Raw("Login is required."), vm.state.value.loginError)
        assertNull(vm.state.value.passwordError)
        assertNull(vm.state.value.banner)
        assertEquals(false, vm.state.value.isSubmitting)
    }

    @Test fun `invalid credentials shows the server message`() = runTest {
        val vm = newVm()
        val message = "The username/email or password is incorrect."
        fake.loginResult = AppResult.Failure(AppError.Business("invalid_credentials", message, null))
        vm.fill()
        vm.onAction(Submit)
        assertEquals(UiText.Raw(message), vm.state.value.banner)
    }

    @Test fun `html 404 shows shop not connected banner`() = runTest {
        val vm = newVm()
        fake.loginResult = AppResult.Failure(AppError.Server(404, null))
        vm.fill()
        vm.onAction(Submit)
        assertEquals(UiText.Res(UiRes.string.error_shop_not_connected), vm.state.value.banner)
    }

    @Test fun `offline shows offline banner`() = runTest {
        val vm = newVm()
        fake.loginResult = AppResult.Failure(AppError.Offline)
        vm.fill()
        vm.onAction(Submit)
        assertEquals(UiText.Res(UiRes.string.error_offline), vm.state.value.banner)
    }

    @Test fun `expired notice shows banner`() = runTest {
        fake.state.value = SessionState.LoggedOut(TestData.smf, SessionNotice.SessionExpired)
        assertEquals(UiText.Res(UiRes.string.session_expired), newVm().state.value.banner)
    }

    @Test fun `typing clears the field error`() = runTest {
        val vm = newVm()
        vm.onAction(Submit)
        vm.onAction(LoginChanged("a"))
        assertNull(vm.state.value.loginError)
        assertEquals(UiText.Res(AuthRes.string.login_error_password_required), vm.state.value.passwordError)
    }

    @Test fun `change shop switches shop`() = runTest {
        val vm = newVm()
        vm.onAction(ChangeShop)
        assertEquals(1, fake.switchShopCalls)
    }

    @Test fun `logging out again clears the password and keeps the login`() = runTest {
        val vm = newVm()
        vm.fill()
        fake.state.value = SessionState.LoggedIn(TestData.smf, TestData.userSession())
        fake.state.value = SessionState.LoggedOut(TestData.smf, null)
        assertEquals("", vm.state.value.password)
        assertEquals("cashier1", vm.state.value.login)
        assertEquals(false, vm.state.value.isSubmitting)
    }

    @Test fun `a different shop clears the login too`() = runTest {
        val vm = newVm()
        vm.onAction(LoginChanged("cashier1"))
        fake.state.value = SessionState.LoggedOut(TestData.local, null)
        assertEquals("", vm.state.value.login)
        assertEquals("Local API (dev)", vm.state.value.shopName)
        assertEquals("local", vm.state.value.shopId)
        assertEquals("local", vm.state.value.shopHost)
    }

    @Test fun `a successful login keeps the button loading while home takes over`() = runTest {
        val vm = newVm()
        vm.fill()
        vm.onAction(Submit)
        assertEquals(1, fake.loginCalls.size)
        assertTrue(vm.state.value.isSubmitting)
    }

    @Test fun `logging out after a successful login brings the button back`() = runTest {
        val vm = newVm()
        vm.fill()
        vm.onAction(Submit)
        fake.state.value = SessionState.LoggedIn(TestData.smf, TestData.userSession())
        fake.state.value = SessionState.LoggedOut(TestData.smf, null)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun `switch shop is ignored while a login is running`() = runTest {
        val vm = newVm()
        fake.loginGate = CompletableDeferred()
        vm.fill()
        vm.onAction(Submit)
        vm.onAction(ChangeShop)
        assertEquals(0, fake.switchShopCalls)
        fake.loginGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(0, fake.switchShopCalls)
    }

    @Test fun `switch shop is ignored after a successful login while home takes over`() = runTest {
        val vm = newVm()
        vm.fill()
        vm.onAction(Submit)
        vm.onAction(ChangeShop)
        assertEquals(0, fake.switchShopCalls)
    }
}
