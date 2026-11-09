package pk.paksoft.pos.core.network.api

import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.dto.BootstrapDto

interface BootstrapApi {
    suspend fun bootstrap(): AppResult<BootstrapDto>
}
