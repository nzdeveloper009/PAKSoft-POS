package pk.paksoft.pos.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShopsTest {
    @Test fun `release build has exactly the three shops`() {
        assertEquals(listOf("smf", "bsf", "mummyandme"), Shops.forBuild(false, "http://x").map { it.id })
    }

    @Test fun `debug build adds staging and local after them`() {
        val shops = Shops.forBuild(true, "http://10.0.2.2:8010/api/v1")
        assertEquals(listOf("smf", "bsf", "mummyandme", "staging", "local"), shops.map { it.id })
        assertEquals("http://10.0.2.2:8010/api/v1", shops.last().apiBaseUrl)
        assertTrue(shops.last().isDevelopment)
    }

    @Test fun `smf url is exact`() {
        assertEquals("https://pbsf.org.pk/smf/api/v1", Shops.production.first { it.id == "smf" }.apiBaseUrl)
    }
}
