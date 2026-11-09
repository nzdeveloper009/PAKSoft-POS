package pk.paksoft.pos.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.data.mapper.toEntities
import pk.paksoft.pos.core.data.mapper.toModel
import pk.paksoft.pos.core.database.dao.BootstrapDao
import pk.paksoft.pos.core.model.ShopSettings
import pk.paksoft.pos.core.network.api.BootstrapApi

@Singleton
class DefaultBootstrapRepository(
    private val api: BootstrapApi,
    private val dao: BootstrapDao,
    private val epoch: SessionEpoch,
) : BootstrapRepository {
    override val settings: Flow<ShopSettings?> = dao.observeSettings().map { it?.toModel() }

    /** An answer that arrives after logout or switch shop is dropped (see [SessionEpoch]). */
    override suspend fun refresh(): AppResult<Unit> {
        val startedAt = epoch.current()
        val dto = when (val result = api.bootstrap()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val e = try {
            dto.toEntities()
        } catch (error: IllegalArgumentException) {
            return AppResult.Failure(AppError.Unexpected(error))
        }
        val written = epoch.writeIfCurrent(startedAt) {
            dao.replaceAll(
                e.settings, e.warehouses, e.billers, e.customerGroups, e.categories,
                e.brands, e.units, e.taxes, e.coupons, e.accounts,
            )
        }
        return if (written) AppResult.Success(Unit) else AppResult.Failure(AppError.Unexpected(null))
    }
}
