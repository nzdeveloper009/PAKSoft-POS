package pk.paksoft.pos.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ShopHostTest {
    @Test fun `a shop under a path keeps its path without the api suffix`() {
        assertEquals("pbsf.org.pk/smf", shopHost("https://pbsf.org.pk/smf/api/v1"))
    }

    @Test fun `a shop on its own host shows only the host`() {
        assertEquals("pos.mummyandme.com.pk", shopHost("https://pos.mummyandme.com.pk/api/v1"))
    }

    @Test fun `a local server keeps its port`() {
        assertEquals("10.0.2.2:8000", shopHost("http://10.0.2.2:8000/api/v1"))
    }

    @Test fun `a trailing slash is dropped`() {
        assertEquals("pbsf.org.pk/smf", shopHost("https://pbsf.org.pk/smf/api/v1/"))
    }
}
