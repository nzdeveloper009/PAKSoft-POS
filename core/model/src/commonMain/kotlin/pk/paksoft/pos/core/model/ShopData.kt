package pk.paksoft.pos.core.model

data class ShopSettings(
    val title: String,
    val logoUrl: String?,
    val developedBy: String?,
    val dateFormat: String,
    val invoiceFormat: String?,
    val gstState: Int?,
    val timezone: String,
    val currency: Currency?,
    val features: ShopFeatures,
    val pos: PosDefaults,
)

data class Currency(
    val id: Long,
    val code: String,
    val name: String,
    val exchangeRate: Double,
    val position: CurrencyPosition,
)

enum class CurrencyPosition { Prefix, Suffix, Unknown }

data class ShopFeatures(val ledger: Boolean, val previousBalance: Boolean)

data class PosDefaults(
    val customerId: Long?,
    val warehouseId: Long?,
    val billerId: Long?,
    val pageSize: Int?,
    val keyboardActive: Boolean,
)
