package pk.paksoft.pos.core.testing

import pk.paksoft.pos.core.model.AppUser
import pk.paksoft.pos.core.model.Currency
import pk.paksoft.pos.core.model.CurrencyPosition
import pk.paksoft.pos.core.model.PosDefaults
import pk.paksoft.pos.core.model.Shop
import pk.paksoft.pos.core.model.ShopFeatures
import pk.paksoft.pos.core.model.ShopSettings
import pk.paksoft.pos.core.model.Shops
import pk.paksoft.pos.core.model.StaffScope
import pk.paksoft.pos.core.model.UserSession
import pk.paksoft.pos.core.network.dto.AccountDto
import pk.paksoft.pos.core.network.dto.BillerDto
import pk.paksoft.pos.core.network.dto.BootstrapDto
import pk.paksoft.pos.core.network.dto.BrandDto
import pk.paksoft.pos.core.network.dto.CategoryDto
import pk.paksoft.pos.core.network.dto.CouponDto
import pk.paksoft.pos.core.network.dto.CurrencyDto
import pk.paksoft.pos.core.network.dto.CustomerGroupDto
import pk.paksoft.pos.core.network.dto.FeaturesDto
import pk.paksoft.pos.core.network.dto.PosDto
import pk.paksoft.pos.core.network.dto.RoleDto
import pk.paksoft.pos.core.network.dto.SessionDto
import pk.paksoft.pos.core.network.dto.ShopDto
import pk.paksoft.pos.core.network.dto.TaxDto
import pk.paksoft.pos.core.network.dto.UnitDto
import pk.paksoft.pos.core.network.dto.UserDto
import pk.paksoft.pos.core.network.dto.WarehouseDto

/** Test values mirroring the docs/API.md examples (A1, A3, A4). */
object TestData {
    private val debugShops = Shops.forBuild(true, "http://local")

    val smf: Shop = debugShops.first { it.id == "smf" }
    val local: Shop = debugShops.first { it.id == "local" }

    /** The A1 login payload; `token = null` gives the A3 `/me` payload. */
    fun sessionDto(
        token: String? = "tok-1",
        permissions: List<String> = listOf("sales-index", "sales-add"),
        roleId: Int = 1,
    ): SessionDto = SessionDto(
        token = token,
        user = UserDto(
            id = 7,
            name = "cashier1",
            email = "cashier1@example.com",
            phone = "0300 0000000",
            companyName = null,
            role = RoleDto(roleId, roleName(roleId)),
            warehouseId = null,
            billerId = null,
        ),
        permissions = permissions,
        staffScope = "all",
    )

    fun userSession(
        name: String = "cashier1",
        roleName: String = "Admin",
        permissions: Set<String> = setOf("sales-index", "sales-add"),
    ): UserSession = UserSession(
        user = AppUser(
            id = 7,
            name = name,
            email = "cashier1@example.com",
            phone = "0300 0000000",
            companyName = null,
            roleId = 1,
            roleName = roleName,
            warehouseId = null,
            billerId = null,
        ),
        permissions = permissions,
        staffScope = StaffScope.All,
    )

    /** The A4 bootstrap payload with [title] and `features.ledger` substituted. */
    fun bootstrapDto(title: String = "Super madina Fan", ledger: Boolean = true): BootstrapDto = BootstrapDto(
        shop = ShopDto(
            title = title,
            logoUrl = "https://pbsf.org.pk/smf/public/logo/logo.png",
            developedBy = "PAKSOFT Solutions",
            dateFormat = "d/m/Y",
            invoiceFormat = "standard",
            gstState = null,
            timezone = "Asia/Karachi",
            currency = CurrencyDto(id = 3, code = "PKR", name = "Pakistani Rupee", exchangeRate = 1.0, position = "prefix"),
            features = FeaturesDto(ledger = ledger, previousBalance = true),
        ),
        pos = PosDto(
            defaultCustomerId = 1,
            defaultWarehouseId = 1,
            defaultBillerId = 1,
            pageSize = 20,
            keyboardActive = false,
        ),
        warehouses = listOf(
            WarehouseDto(id = 1, name = "Super Madina Fan", phone = null, email = null, address = "Lahore", isActive = true),
        ),
        billers = listOf(
            BillerDto(
                id = 1,
                name = "Admin",
                companyName = "BSF",
                phoneNumber = "0300…",
                email = "…",
                address = "…",
                city = "Lahore",
                imageUrl = null,
                isActive = true,
            ),
        ),
        customerGroups = listOf(CustomerGroupDto(id = 1, name = "General", percentage = 0.0, isActive = true)),
        categories = listOf(CategoryDto(id = 1, name = "Product", parentId = null, imageUrl = null, isActive = true)),
        brands = listOf(BrandDto(id = 1, title = "SMF", imageUrl = null, isActive = true)),
        units = listOf(
            UnitDto(
                id = 1,
                code = "pc",
                name = "Piece",
                baseUnitId = null,
                operator = "*",
                operationValue = 1.0,
                isActive = true,
            ),
        ),
        taxes = listOf(TaxDto(id = 1, name = "GST 17%", rate = 17.0, isActive = true)),
        coupons = listOf(
            CouponDto(
                id = 2,
                code = "EID10",
                type = "percentage",
                amount = "10.00",
                minimumAmount = null,
                remaining = 40,
                expiredDate = "2026-12-31",
            ),
        ),
        accounts = listOf(AccountDto(id = 1, accountNo = "11111", name = "Sales Account", isDefault = true)),
    )

    /** The settings that [bootstrapDto] maps to. */
    fun shopSettings(ledger: Boolean = true): ShopSettings = ShopSettings(
        title = "Super madina Fan",
        logoUrl = "https://pbsf.org.pk/smf/public/logo/logo.png",
        developedBy = "PAKSOFT Solutions",
        dateFormat = "d/m/Y",
        invoiceFormat = "standard",
        gstState = null,
        timezone = "Asia/Karachi",
        currency = Currency(id = 3, code = "PKR", name = "Pakistani Rupee", exchangeRate = 1.0, position = CurrencyPosition.Prefix),
        features = ShopFeatures(ledger = ledger, previousBalance = true),
        pos = PosDefaults(customerId = 1, warehouseId = 1, billerId = 1, pageSize = 20, keyboardActive = false),
    )

    // DOMAIN.md §2 role ids.
    private fun roleName(roleId: Int): String = when (roleId) {
        1 -> "Admin"
        2 -> "Owner"
        5 -> "Customer"
        else -> "staff"
    }
}
