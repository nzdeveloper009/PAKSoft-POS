package pk.paksoft.pos.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pk.paksoft.pos.core.data.mapper.toEntities
import pk.paksoft.pos.core.data.mapper.toModel
import pk.paksoft.pos.core.model.CurrencyPosition
import pk.paksoft.pos.core.model.Money
import pk.paksoft.pos.core.network.dto.CouponDto
import pk.paksoft.pos.core.testing.TestData

class BootstrapMappersTest {
    private fun coupon(id: Long, type: String, amount: String, minimum: Money? = null, expiredDate: String? = "2026-12-31") =
        CouponDto(id, "C$id", type, amount, minimum, remaining = 5, expiredDate = expiredDate)

    private fun couponsOf(vararg coupons: CouponDto) =
        TestData.bootstrapDto().copy(coupons = coupons.toList()).toEntities().coupons

    @Test
    fun `percentage coupon keeps hundredths of a percent`() {
        val entity = couponsOf(coupon(2, "percentage", "10.00")).single()
        assertEquals(1000, entity.amountHundredths)
        assertEquals("percentage", entity.type)
    }

    @Test
    fun `fixed coupon becomes paisa`() {
        val entity = couponsOf(coupon(3, "fixed", "250.50", minimum = Money(100_000))).single()
        assertEquals(25050, entity.amountHundredths)
        assertEquals(100_000, entity.minimumAmountPaisa)
        assertEquals("2026-12-31", entity.expiredDate)
    }

    @Test
    fun `coupon without an expiry date is dropped`() {
        val entities = couponsOf(coupon(4, "fixed", "100.00", expiredDate = null), coupon(5, "percentage", "5.00"))
        assertEquals(listOf(5L), entities.map { it.id })
    }

    @Test
    fun `settings entity round trips to model`() {
        val model = TestData.bootstrapDto().toEntities().settings.toModel()
        assertEquals("Super madina Fan", model.title)
        assertEquals("PKR", model.currency?.code)
        assertEquals(CurrencyPosition.Prefix, model.currency?.position)
        assertTrue(model.features.ledger)
        assertEquals(20, model.pos.pageSize)
        assertEquals(TestData.shopSettings(), model)
    }

    @Test
    fun `gst settings keep invoice format and state`() {
        val dto = TestData.bootstrapDto().let { it.copy(shop = it.shop.copy(invoiceFormat = "gst", gstState = 2)) }
        val model = dto.toEntities().settings.toModel()
        assertEquals("gst", model.invoiceFormat)
        assertEquals(2, model.gstState)
    }

    @Test
    fun `currency position suffix unknown and missing`() {
        fun positionOf(position: String) = TestData.bootstrapDto()
            .let { it.copy(shop = it.shop.copy(currency = it.shop.currency?.copy(position = position))) }
            .toEntities().settings.toModel().currency?.position
        assertEquals(CurrencyPosition.Suffix, positionOf("suffix"))
        assertEquals(CurrencyPosition.Unknown, positionOf("middle"))

        val noCurrency = TestData.bootstrapDto().let { it.copy(shop = it.shop.copy(currency = null)) }
        assertNull(noCurrency.toEntities().settings.toModel().currency)
    }

    @Test
    fun `every reference list is mapped`() {
        val e = TestData.bootstrapDto().toEntities()
        assertEquals("Lahore", e.warehouses.single().address)
        assertEquals("BSF", e.billers.single().companyName)
        assertEquals("General", e.customerGroups.single().name)
        assertEquals("Product", e.categories.single().name)
        assertEquals("SMF", e.brands.single().title)
        assertEquals("*", e.units.single().operator)
        assertEquals(17.0, e.taxes.single().rate)
        assertEquals("EID10", e.coupons.single().code)
        assertEquals("11111", e.accounts.single().accountNo)
    }
}
