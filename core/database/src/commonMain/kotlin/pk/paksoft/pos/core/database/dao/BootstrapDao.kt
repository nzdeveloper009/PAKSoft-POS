package pk.paksoft.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
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

@Dao
abstract class BootstrapDao {
    /** Atomically swaps the whole cache for a fresh bootstrap payload. */
    @Transaction
    open suspend fun replaceAll(
        settings: ShopSettingsEntity,
        warehouses: List<WarehouseEntity>,
        billers: List<BillerEntity>,
        customerGroups: List<CustomerGroupEntity>,
        categories: List<CategoryEntity>,
        brands: List<BrandEntity>,
        units: List<UnitEntity>,
        taxes: List<TaxEntity>,
        coupons: List<CouponEntity>,
        accounts: List<AccountEntity>,
    ) {
        clearAll()
        insertSettings(settings)
        insertWarehouses(warehouses)
        insertBillers(billers)
        insertCustomerGroups(customerGroups)
        insertCategories(categories)
        insertBrands(brands)
        insertUnits(units)
        insertTaxes(taxes)
        insertCoupons(coupons)
        insertAccounts(accounts)
    }

    @Transaction
    open suspend fun clearAll() {
        deleteSettings()
        deleteWarehouses()
        deleteBillers()
        deleteCustomerGroups()
        deleteCategories()
        deleteBrands()
        deleteUnits()
        deleteTaxes()
        deleteCoupons()
        deleteAccounts()
    }

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    abstract fun observeSettings(): Flow<ShopSettingsEntity?>

    @Query("SELECT * FROM warehouses ORDER BY id")
    abstract fun observeWarehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM billers ORDER BY id")
    abstract fun observeBillers(): Flow<List<BillerEntity>>

    @Query(
        "SELECT (SELECT COUNT(*) FROM shop_settings) + (SELECT COUNT(*) FROM warehouses) + " +
            "(SELECT COUNT(*) FROM billers) + (SELECT COUNT(*) FROM customer_groups) + " +
            "(SELECT COUNT(*) FROM categories) + (SELECT COUNT(*) FROM brands) + " +
            "(SELECT COUNT(*) FROM units) + (SELECT COUNT(*) FROM taxes) + " +
            "(SELECT COUNT(*) FROM coupons) + (SELECT COUNT(*) FROM accounts)",
    )
    abstract suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertSettings(settings: ShopSettingsEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertWarehouses(rows: List<WarehouseEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertBillers(rows: List<BillerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertCustomerGroups(rows: List<CustomerGroupEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertCategories(rows: List<CategoryEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertBrands(rows: List<BrandEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertUnits(rows: List<UnitEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertTaxes(rows: List<TaxEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertCoupons(rows: List<CouponEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertAccounts(rows: List<AccountEntity>)

    @Query("DELETE FROM shop_settings") protected abstract suspend fun deleteSettings()
    @Query("DELETE FROM warehouses") protected abstract suspend fun deleteWarehouses()
    @Query("DELETE FROM billers") protected abstract suspend fun deleteBillers()
    @Query("DELETE FROM customer_groups") protected abstract suspend fun deleteCustomerGroups()
    @Query("DELETE FROM categories") protected abstract suspend fun deleteCategories()
    @Query("DELETE FROM brands") protected abstract suspend fun deleteBrands()
    @Query("DELETE FROM units") protected abstract suspend fun deleteUnits()
    @Query("DELETE FROM taxes") protected abstract suspend fun deleteTaxes()
    @Query("DELETE FROM coupons") protected abstract suspend fun deleteCoupons()
    @Query("DELETE FROM accounts") protected abstract suspend fun deleteAccounts()
}
