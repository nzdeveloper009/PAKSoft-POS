package pk.paksoft.pos.core.testing

import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.api.BootstrapApi
import pk.paksoft.pos.core.network.dto.BootstrapDto

class FakeBootstrapApi(
    var result: AppResult<BootstrapDto> = AppResult.Success(TestData.bootstrapDto()),
) : BootstrapApi {
    override suspend fun bootstrap(): AppResult<BootstrapDto> = result
}
