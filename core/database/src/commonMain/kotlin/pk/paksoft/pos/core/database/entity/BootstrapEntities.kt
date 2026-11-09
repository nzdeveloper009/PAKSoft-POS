package pk.paksoft.pos.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table (id is always 1): shop settings plus POS defaults from bootstrap. */
@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val title: String,
    val logoUrl: String?,
    val developedBy: String?,
    val dateFormat: String,
    val invoiceFormat: String,
    val gstState: Int?,
    val timezone: String,
    val currencyId: Long?,
    val currencyCode: String?,
    val currencyName: String?,
    val exchangeRate: Double?,
    val currencyPosition: String?,
    val featureLedger: Boolean,
    val featurePreviousBalance: Boolean,
    val posCustomerId: Long?,
    val posWarehouseId: Long?,
    val posBillerId: Long?,
    val posPageSize: Int?,
    val posKeyboardActive: Boolean,
)

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val isActive: Boolean,
)

@Entity(tableName = "billers")
data class BillerEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val companyName: String?,
    val phoneNumber: String?,
    val email: String?,
    val address: String?,
    val city: String?,
    val imageUrl: String?,
    val isActive: Boolean,
)

@Entity(tableName = "customer_groups")
data class CustomerGroupEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val percentage: Double,
    val isActive: Boolean,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val parentId: Long?,
    val imageUrl: String?,
    val isActive: Boolean,
)

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val imageUrl: String?,
    val isActive: Boolean,
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: Long,
    val code: String,
    val name: String,
    val baseUnitId: Long?,
    val operator: String?,
    val operationValue: Double?,
    val isActive: Boolean,
)

@Entity(tableName = "taxes")
data class TaxEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val rate: Double,
    val isActive: Boolean,
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: Long,
    val code: String,
    val type: String,
    /** Fixed amount in paisa, or percentage x 100, depending on [type]. */
    val amountHundredths: Long,
    val minimumAmountPaisa: Long?,
    val remaining: Int,
    val expiredDate: String,
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val accountNo: String,
    val name: String,
    val isDefault: Boolean,
)
