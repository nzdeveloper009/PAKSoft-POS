package pk.paksoft.pos.core.data.mapper

import kotlin.math.roundToLong
import pk.paksoft.pos.core.database.entity.AccountEntity
import pk.paksoft.pos.core.database.entity.BillerEntity
import pk.paksoft.pos.core.database.entity.BrandEntity
import pk.paksoft.pos.core.database.entity.CategoryEntity
import pk.paksoft.pos.core.database.entity.CouponEntity
import pk.paksoft.pos.core.database.entity.CustomerGroupEntity
import pk.paksoft.pos.core.database.entity.ShopSettingsEntity
import pk.paksoft.pos.core.database.entity.TaxEntity
import pk.paksoft.pos.core.database.entity.UnitEntity
import pk.paksoft.pos.core.database.entity.WarehouseEntity
import pk.paksoft.pos.core.model.Currency
import pk.paksoft.pos.core.model.CurrencyPosition
import pk.paksoft.pos.core.model.Money
import pk.paksoft.pos.core.model.PosDefaults
import pk.paksoft.pos.core.model.ShopFeatures
import pk.paksoft.pos.core.model.ShopSettings
import pk.paksoft.pos.core.network.dto.BootstrapDto
import pk.paksoft.pos.core.network.dto.CouponDto

/** One bootstrap payload as rows for `BootstrapDao.replaceAll`. */
internal data class BootstrapEntities(
    val settings: ShopSettingsEntity,
    val warehouses: List<WarehouseEntity>,
    val billers: List<BillerEntity>,
    val customerGroups: List<CustomerGroupEntity>,
    val categories: List<CategoryEntity>,
    val brands: List<BrandEntity>,
    val units: List<UnitEntity>,
    val taxes: List<TaxEntity>,
    val coupons: List<CouponEntity>,
    val accounts: List<AccountEntity>,
)

/** Throws [IllegalArgumentException] when a coupon amount is not a decimal number. */
internal fun BootstrapDto.toEntities(): BootstrapEntities = BootstrapEntities(
    settings = ShopSettingsEntity(
        title = shop.title,
        logoUrl = shop.logoUrl,
        developedBy = shop.developedBy,
        dateFormat = shop.dateFormat,
        invoiceFormat = shop.invoiceFormat,
        gstState = shop.gstState,
        timezone = shop.timezone,
        currencyId = shop.currency?.id,
        currencyCode = shop.currency?.code,
        currencyName = shop.currency?.name,
        exchangeRate = shop.currency?.exchangeRate,
        currencyPosition = shop.currency?.position,
        featureLedger = shop.features.ledger,
        featurePreviousBalance = shop.features.previousBalance,
        posCustomerId = pos.defaultCustomerId,
        posWarehouseId = pos.defaultWarehouseId,
        posBillerId = pos.defaultBillerId,
        posPageSize = pos.pageSize,
        posKeyboardActive = pos.keyboardActive,
    ),
    warehouses = warehouses.map {
        WarehouseEntity(it.id, it.name, it.phone, it.email, it.address, it.isActive)
    },
    billers = billers.map {
        BillerEntity(it.id, it.name, it.companyName, it.phoneNumber, it.email, it.address, it.city, it.imageUrl, it.isActive)
    },
    customerGroups = customerGroups.map { CustomerGroupEntity(it.id, it.name, it.percentage, it.isActive) },
    categories = categories.map { CategoryEntity(it.id, it.name, it.parentId, it.imageUrl, it.isActive) },
    brands = brands.map { BrandEntity(it.id, it.title, it.imageUrl, it.isActive) },
    units = units.map {
        UnitEntity(it.id, it.code, it.name, it.baseUnitId, it.operator, it.operationValue, it.isActive)
    },
    taxes = taxes.map { TaxEntity(it.id, it.name, it.rate, it.isActive) },
    coupons = coupons.mapNotNull { it.toEntity() },
    accounts = accounts.map { AccountEntity(it.id, it.accountNo, it.name, it.isDefault) },
)

/**
 * A coupon without `expired_date` is dropped: its validity can't be checked.
 * Legacy: pos.blade.php couponDiscount (`fixed` is an amount off, any other type a percentage).
 */
private fun CouponDto.toEntity(): CouponEntity? {
    val expiry = expiredDate ?: return null
    val hundredths = if (type == "fixed") Money.parse(amount).paisa else (amount.toDouble() * 100).roundToLong()
    return CouponEntity(
        id = id,
        code = code,
        type = type,
        amountHundredths = hundredths,
        minimumAmountPaisa = minimumAmount?.paisa,
        remaining = remaining,
        expiredDate = expiry,
    )
}

internal fun ShopSettingsEntity.toModel(): ShopSettings = ShopSettings(
    title = title,
    logoUrl = logoUrl,
    developedBy = developedBy,
    dateFormat = dateFormat,
    invoiceFormat = invoiceFormat,
    gstState = gstState,
    timezone = timezone,
    currency = currency(),
    features = ShopFeatures(ledger = featureLedger, previousBalance = featurePreviousBalance),
    pos = PosDefaults(
        customerId = posCustomerId,
        warehouseId = posWarehouseId,
        billerId = posBillerId,
        pageSize = posPageSize,
        keyboardActive = posKeyboardActive,
    ),
)

private fun ShopSettingsEntity.currency(): Currency? {
    val id = currencyId ?: return null
    val code = currencyCode ?: return null
    val name = currencyName ?: return null
    val rate = exchangeRate ?: return null
    val position = when (currencyPosition) {
        "prefix" -> CurrencyPosition.Prefix
        "suffix" -> CurrencyPosition.Suffix
        else -> CurrencyPosition.Unknown
    }
    return Currency(id, code, name, rate, position)
}
