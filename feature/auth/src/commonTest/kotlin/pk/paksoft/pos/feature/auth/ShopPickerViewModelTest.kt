package pk.paksoft.pos.feature.auth

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pk.paksoft.pos.core.testing.FakeSessionRepository
import pk.paksoft.pos.feature.auth.shop.ShopPickerAction
import pk.paksoft.pos.feature.auth.shop.ShopPickerViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ShopPickerViewModelTest {
    private val fake = FakeSessionRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun `lists every shop with dev flag`() = runTest {
        val vm = ShopPickerViewModel(fake)
        val shops = vm.state.value.shops
        assertEquals(listOf("smf", "bsf", "mummyandme", "demo", "staging", "local"), shops.map { it.id })
        assertTrue(shops.first { it.id == "local" }.isDevelopment)
        assertEquals(false, shops.first { it.id == "smf" }.isDevelopment)
    }

    @Test fun `clicking a shop selects it`() = runTest {
        val vm = ShopPickerViewModel(fake)
        vm.onAction(ShopPickerAction.ShopClicked("bsf"))
        assertEquals(listOf("bsf"), fake.selectedShopIds)
    }
}
