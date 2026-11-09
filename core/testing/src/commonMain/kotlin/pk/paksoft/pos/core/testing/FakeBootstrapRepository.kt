package pk.paksoft.pos.core.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.BootstrapRepository
import pk.paksoft.pos.core.model.ShopSettings

class FakeBootstrapRepository : BootstrapRepository {
    val settingsFlow: MutableStateFlow<ShopSettings?> = MutableStateFlow(null)
    override val settings: Flow<ShopSettings?> = settingsFlow

    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls: Int = 0
        private set

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }
}
