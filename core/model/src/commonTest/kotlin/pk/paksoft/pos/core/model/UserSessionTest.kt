package pk.paksoft.pos.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserSessionTest {
    @Test fun `can checks permission names`() {
        val user = AppUser(1, "Ali", "ali@example.com", null, null, 2, "Cashier", null, null)
        val session = UserSession(user, setOf(Permissions.SALES_INDEX), StaffScope.Own)
        assertTrue(session.can("sales-index"))
        assertFalse(session.can("sales-add"))
    }
}
