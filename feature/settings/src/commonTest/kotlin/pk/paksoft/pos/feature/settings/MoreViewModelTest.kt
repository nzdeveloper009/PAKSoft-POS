package pk.paksoft.pos.feature.settings

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.designsystem.theme.ShopBrand
import pk.paksoft.pos.core.model.SessionState
import pk.paksoft.pos.core.testing.FakeBootstrapRepository
import pk.paksoft.pos.core.testing.FakeSessionRepository
import pk.paksoft.pos.core.testing.TestData
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.resources.error_offline
import pk.paksoft.pos.feature.settings.resources.more_refreshed
import pk.paksoft.pos.core.ui.resources.Res as UiRes
import pk.paksoft.pos.feature.settings.resources.Res

@OptIn(ExperimentalCoroutinesApi::class)
class MoreViewModelTest {
    private val session = FakeSessionRepository(
        SessionState.LoggedIn(TestData.smf, TestData.userSession(name = "cashier1", roleName = "Admin")),
    )
    private val bootstrap = FakeBootstrapRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun `shows user role and shop title`() = runTest {
        bootstrap.settingsFlow.value = TestData.shopSettings()
        val vm = MoreViewModel(session, bootstrap)
        val state = vm.state.value
        assertEquals("cashier1", state.userName)
        assertEquals("Admin", state.roleName)
        assertEquals("Super madina Fan", state.shopName)
    }

    @Test fun `shows the shop id and host`() = runTest {
        val state = MoreViewModel(session, bootstrap).state.value
        assertEquals("smf", state.shopId)
        assertEquals("pbsf.org.pk/smf", state.shopHost)
    }

    @Test fun `logging out keeps the last user and shop while the screen leaves`() = runTest {
        bootstrap.settingsFlow.value = TestData.shopSettings()
        val vm = MoreViewModel(session, bootstrap)
        // Logout clears the shop data first, then the session ends; More is still animating out meanwhile.
        bootstrap.settingsFlow.value = null
        session.state.value = SessionState.LoggedOut(TestData.smf, null)
        val state = vm.state.value
        assertEquals("cashier1", state.userName)
        assertEquals("Admin", state.roleName)
        assertEquals("Super madina Fan", state.shopName)
        assertEquals("smf", state.shopId)
        assertEquals("pbsf.org.pk/smf", state.shopHost)
    }

    @Test fun `switching shop keeps the last user and shop while the screen leaves`() = runTest {
        val vm = MoreViewModel(session, bootstrap)
        session.state.value = SessionState.NoShop
        val state = vm.state.value
        assertEquals("cashier1", state.userName)
        assertEquals("Super Madina Solar Fan", state.shopName)
        assertEquals("smf", state.shopId)
    }

    @Test fun `signing in to another shop after the hold drops the old user and title`() = runTest {
        bootstrap.settingsFlow.value = TestData.shopSettings()
        val vm = MoreViewModel(session, bootstrap)
        bootstrap.settingsFlow.value = null
        session.state.value = SessionState.LoggedOut(TestData.smf, null)
        val owner = TestData.userSession(name = "owner", roleName = "Owner")
        session.state.value = SessionState.LoggedIn(TestData.local, owner)
        val state = vm.state.value
        assertEquals("owner", state.userName)
        assertEquals("Owner", state.roleName)
        assertEquals("local", state.shopId)
        assertEquals("Local API (dev)", state.shopName)
        assertEquals("Local API (dev)", state.shopDisplayName)
        assertEquals("local", state.shopHost)
    }

    @Test fun `the avatar identity comes from the shop list name and not the bootstrap title`() = runTest {
        session.state.value = SessionState.LoggedIn(TestData.local, TestData.userSession())
        bootstrap.settingsFlow.value = TestData.shopSettings()
        val state = MoreViewModel(session, bootstrap).state.value
        assertEquals("Super madina Fan", state.shopName)
        assertEquals("Local API (dev)", state.shopDisplayName)
        // The same inputs the shop picker and login use, so the three show the same identity ("LO").
        assertEquals(
            ShopBrand.forShop(TestData.local.id, TestData.local.displayName),
            ShopBrand.forShop(state.shopId, state.shopDisplayName),
        )
    }

    @Test fun `falls back to shop display name without cached settings`() = runTest {
        val vm = MoreViewModel(session, bootstrap)
        assertEquals("Super Madina Solar Fan", vm.state.value.shopName)
    }

    @Test fun `refresh success shows updated message`() = runTest {
        val vm = MoreViewModel(session, bootstrap)
        vm.onAction(MoreAction.RefreshData)
        assertEquals(UiText.Res(Res.string.more_refreshed), vm.state.value.message)
        assertEquals(false, vm.state.value.isRefreshing)
        assertEquals(false, vm.state.value.isMessageError)
        assertEquals(1, bootstrap.refreshCalls)
    }

    @Test fun `refresh failure shows error`() = runTest {
        bootstrap.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = MoreViewModel(session, bootstrap)
        vm.onAction(MoreAction.RefreshData)
        assertEquals(UiText.Res(UiRes.string.error_offline), vm.state.value.message)
        assertEquals(true, vm.state.value.isMessageError)
        assertEquals(false, vm.state.value.isRefreshing)
    }

    @Test fun `second refresh while refreshing is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val gated = object : pk.paksoft.pos.core.data.BootstrapRepository by bootstrap {
            override suspend fun refresh(): AppResult<Unit> {
                bootstrap.refresh()
                gate.await()
                return AppResult.Success(Unit)
            }
        }
        val vm = MoreViewModel(session, gated)
        vm.onAction(MoreAction.RefreshData)
        assertEquals(true, vm.state.value.isRefreshing)
        vm.onAction(MoreAction.RefreshData)
        gate.complete(Unit)
        assertEquals(1, bootstrap.refreshCalls)
        assertEquals(false, vm.state.value.isRefreshing)
    }

    @Test fun `logout and switch delegate to session`() = runTest {
        val vm = MoreViewModel(session, bootstrap)
        vm.onAction(MoreAction.Logout)
        assertEquals(1, session.logoutCalls)
        vm.onAction(MoreAction.SwitchShop)
        assertEquals(1, session.switchShopCalls)
    }
}
