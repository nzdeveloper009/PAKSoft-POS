package pk.paksoft.pos.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShopsTest {
    @Test fun `release build has exactly the three shops`() {
        assertEquals(listOf("smf", "bsf", "mummyandme"), Shops.forBuild(false, "http://x").map { it.id })
    }

    @Test fun `debug build adds demo and staging and local after them`() {
        val shops = Shops.forBuild(true, "http://10.0.2.2:8010/api/v1")
        assertEquals(listOf("smf", "bsf", "mummyandme", "demo", "staging", "local"), shops.map { it.id })
        assertEquals("http://10.0.2.2:8010/api/v1", shops.last().apiBaseUrl)
        assertTrue(shops.last().isDevelopment)
    }

    @Test fun `smf url is exact`() {
        assertEquals("https://pbsf.org.pk/smf/api/v1", Shops.production.first { it.id == "smf" }.apiBaseUrl)
    }

    @Test fun `debug builds list the demo shop first among dev shops`() {
        assertEquals(
            listOf("smf", "bsf", "mummyandme", "demo", "staging", "local"),
            Shops.forBuild(true, "http://10.0.2.2/api/v1").map { it.id },
        )
    }

    @Test fun `release builds never list the demo shop`() {
        assertTrue(Shops.forBuild(false, "x").none { it.id == "demo" })
    }
}
