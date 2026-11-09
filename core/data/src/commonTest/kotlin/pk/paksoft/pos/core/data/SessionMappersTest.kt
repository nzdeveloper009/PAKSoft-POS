package pk.paksoft.pos.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import pk.paksoft.pos.core.data.mapper.toUserSession
import pk.paksoft.pos.core.model.AppUser
import pk.paksoft.pos.core.model.StaffScope
import pk.paksoft.pos.core.testing.TestData

class SessionMappersTest {
    @Test
    fun `staff scope own maps to Own`() {
        assertEquals(StaffScope.Own, TestData.sessionDto().copy(staffScope = "own").toUserSession().staffScope)
        assertEquals(StaffScope.All, TestData.sessionDto().copy(staffScope = "all").toUserSession().staffScope)
        assertEquals(StaffScope.Unknown, TestData.sessionDto().copy(staffScope = "weird").toUserSession().staffScope)

        val session = TestData.sessionDto(permissions = listOf("sales-index", "sales-add", "sales-index")).toUserSession()
        assertEquals(setOf("sales-index", "sales-add"), session.permissions)
    }

    @Test
    fun `user fields come from the dto`() {
        val user = TestData.sessionDto(roleId = 4).toUserSession().user
        assertEquals(
            AppUser(
                id = 7,
                name = "cashier1",
                email = "cashier1@example.com",
                phone = "0300 0000000",
                companyName = null,
                roleId = 4,
                roleName = "staff",
                warehouseId = null,
                billerId = null,
            ),
            user,
        )
    }
}
