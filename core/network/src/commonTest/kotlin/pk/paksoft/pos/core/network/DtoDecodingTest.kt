package pk.paksoft.pos.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pk.paksoft.pos.core.network.dto.BootstrapDto
import pk.paksoft.pos.core.network.dto.DataEnvelope
import pk.paksoft.pos.core.network.dto.SessionDto

class DtoDecodingTest {
    private val session = DataEnvelope.serializer(SessionDto.serializer())

    @Test
    fun `login example decodes`() {
        val s = PosJson.decodeFromString(session, ApiFixtures.LOGIN_200).data
        assertEquals("12|Xn3r…", s.token)
        assertEquals(7, s.user.id)
        assertEquals("Admin", s.user.role.name)
        assertEquals("all", s.staffScope)
        assertTrue("sales-add" in s.permissions)
        assertNull(s.user.warehouseId)
    }

    @Test
    fun `me example has no token`() {
        assertNull(PosJson.decodeFromString(session, ApiFixtures.ME_200).data.token)
    }

    @Test
    fun `bootstrap example decodes`() {
        val b = PosJson.decodeFromString(DataEnvelope.serializer(BootstrapDto.serializer()), ApiFixtures.BOOTSTRAP_200).data
        assertEquals("PKR", b.shop.currency?.code)
        assertEquals("prefix", b.shop.currency?.position)
        assertTrue(b.shop.features.ledger)
        assertEquals(20, b.pos.pageSize)
        assertEquals("10.00", b.coupons.single().amount)
        assertNull(b.coupons.single().minimumAmount)
        assertEquals(17.0, b.taxes.single().rate)
    }

    @Test
    fun `unknown keys are ignored`() {
        val withExtra = ApiFixtures.LOGIN_200.replace("\"warehouse_id\": null", "\"warehouse_id\": null, \"new_field\": 1")
        assertTrue(withExtra != ApiFixtures.LOGIN_200)
        assertEquals(7, PosJson.decodeFromString(session, withExtra).data.user.id)
    }
}
