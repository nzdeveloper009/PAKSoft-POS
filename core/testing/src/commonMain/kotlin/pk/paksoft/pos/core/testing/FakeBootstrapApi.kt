package pk.paksoft.pos.core.testing

import kotlinx.coroutines.CompletableDeferred
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.api.BootstrapApi
import pk.paksoft.pos.core.network.dto.BootstrapDto

class FakeBootstrapApi(
    var result: AppResult<BootstrapDto> = AppResult.Success(TestData.bootstrapDto()),
) : BootstrapApi {
    /** When set, [bootstrap] records the call and then suspends until the gate completes. */
    var gate: CompletableDeferred<Unit>? = null

    var calls: Int = 0
        private set

    override suspend fun bootstrap(): AppResult<BootstrapDto> {
        calls++
        gate?.await()
        return result
    }
}
