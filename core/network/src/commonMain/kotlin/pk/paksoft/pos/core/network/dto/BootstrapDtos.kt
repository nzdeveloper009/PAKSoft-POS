package pk.paksoft.pos.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pk.paksoft.pos.core.model.Money
import pk.paksoft.pos.core.network.MoneySerializer

@Serializable
data class BootstrapDto(
    @SerialName("shop") val shop: ShopDto,
    @SerialName("pos") val pos: PosDto,
    @SerialName("warehouses") val warehouses: List<WarehouseDto>,
    @SerialName("billers") val billers: List<BillerDto>,
    @SerialName("customer_groups") val customerGroups: List<CustomerGroupDto>,
    @SerialName("categories") val categories: List<CategoryDto>,
    @SerialName("brands") val brands: List<BrandDto>,
    @SerialName("units") val units: List<UnitDto>,
    @SerialName("taxes") val taxes: List<TaxDto>,
    @SerialName("coupons") val coupons: List<CouponDto>,
    @SerialName("accounts") val accounts: List<AccountDto>,
)

@Serializable
data class ShopDto(
    @SerialName("title") val title: String,
    @SerialName("logo_url") val logoUrl: String?,
    @SerialName("developed_by") val developedBy: String?,
    @SerialName("date_format") val dateFormat: String,
    @SerialName("invoice_format") val invoiceFormat: String,
    @SerialName("gst_state") val gstState: String?,
    @SerialName("timezone") val timezone: String,
    @SerialName("currency") val currency: CurrencyDto?,
    @SerialName("features") val features: FeaturesDto,
)

@Serializable
data class CurrencyDto(
    @SerialName("id") val id: Long,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("exchange_rate") val exchangeRate: Double,
    @SerialName("position") val position: String,
)

@Serializable
data class FeaturesDto(
    @SerialName("ledger") val ledger: Boolean,
    @SerialName("previous_balance") val previousBalance: Boolean,
)

@Serializable
data class PosDto(
    @SerialName("default_customer_id") val defaultCustomerId: Long?,
    @SerialName("default_warehouse_id") val defaultWarehouseId: Long?,
    @SerialName("default_biller_id") val defaultBillerId: Long?,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("keyboard_active") val keyboardActive: Boolean,
)

@Serializable
data class WarehouseDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String?,
    @SerialName("email") val email: String?,
    @SerialName("address") val address: String?,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class BillerDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("company_name") val companyName: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    @SerialName("email") val email: String?,
    @SerialName("address") val address: String?,
    @SerialName("city") val city: String?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class CustomerGroupDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("percentage") val percentage: Double,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("parent_id") val parentId: Long?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class BrandDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class UnitDto(
    @SerialName("id") val id: Long,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("base_unit_id") val baseUnitId: Long?,
    @SerialName("operator") val operator: String?,
    @SerialName("operation_value") val operationValue: Double?,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class TaxDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("rate") val rate: Double,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class CouponDto(
    @SerialName("id") val id: Long,
    @SerialName("code") val code: String,
    @SerialName("type") val type: String,
    /** A String because it is a percentage or a fixed amount depending on [type]. */
    @SerialName("amount") val amount: String,
    @SerialName("minimum_amount") @Serializable(with = MoneySerializer::class) val minimumAmount: Money?,
    @SerialName("remaining") val remaining: Int,
    @SerialName("expired_date") val expiredDate: String?,
)

@Serializable
data class AccountDto(
    @SerialName("id") val id: Long,
    @SerialName("account_no") val accountNo: String,
    @SerialName("name") val name: String,
    @SerialName("is_default") val isDefault: Boolean,
)
