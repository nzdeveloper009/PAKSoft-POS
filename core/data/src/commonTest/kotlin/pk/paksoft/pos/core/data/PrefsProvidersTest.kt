package pk.paksoft.pos.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import pk.paksoft.pos.core.model.AppConfig
import pk.paksoft.pos.core.testing.InMemoryAppPreferences
import pk.paksoft.pos.core.testing.InMemoryTokenStore

class PrefsProvidersTest {
    private val catalog = ShopCatalog(AppConfig(true, "iOS", "18", "0.1.0", "http://127.0.0.1:8010/api/v1"))

    @Test
    fun `base url follows the active shop`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "local")
        val provider = PrefsActiveShopProvider(prefs, catalog)
        assertEquals("http://127.0.0.1:8010/api/v1", provider.currentApiBaseUrl())

        prefs.setActiveShopId("bsf")
        assertEquals("https://pbsf.org.pk/bsf/api/v1", provider.currentApiBaseUrl())

        prefs.setActiveShopId(null)
        assertNull(provider.currentApiBaseUrl())
    }

    @Test
    fun `token is the active shop's and read on every call`() = runTest {
        val prefs = InMemoryAppPreferences(activeShopId = "smf")
        val tokens = InMemoryTokenStore(mapOf("smf" to "a", "bsf" to "b"))
        val provider = StoredTokenProvider(prefs, tokens)
        assertEquals("a", provider.token())

        prefs.setActiveShopId("bsf")
        assertEquals("b", provider.token())

        tokens.clear("bsf")
        assertNull(provider.token())

        prefs.setActiveShopId(null)
        assertNull(provider.token())
    }
}
