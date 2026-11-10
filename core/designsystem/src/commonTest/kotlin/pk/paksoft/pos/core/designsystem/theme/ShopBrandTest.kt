package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ShopBrandTest {

    @Test
    fun `smf is the sunny amber shop`() {
        val b = ShopBrand.forShop("smf", "SMF")
        assertEquals(ShopShape.SUNNY, b.shape)
        assertEquals("SM", b.initials)
        assertEquals(Color(0xFFE8890C), b.light.color)
    }

    @Test
    fun `bsf uses the nine sided cookie`() {
        val b = ShopBrand.forShop("bsf", "BSF")
        assertEquals(ShopShape.COOKIE_9, b.shape)
        assertEquals("BS", b.initials)
    }

    @Test
    fun `mummyandme uses the flower and ampersand initials`() {
        val b = ShopBrand.forShop("mummyandme", "Mummy & Me")
        assertEquals(ShopShape.FLOWER, b.shape)
        assertEquals("M&", b.initials)
    }

    @Test
    fun `demo uses the four leaf clover`() {
        val b = ShopBrand.forShop("demo", "Demo shop")
        assertEquals(ShopShape.CLOVER_4, b.shape)
        assertEquals("DM", b.initials)
    }

    @Test
    fun `other shops get the neutral square with initials from the display name`() {
        val staging = ShopBrand.forShop("staging", "Staging (dev)")
        assertEquals(ShopShape.SQUARE, staging.shape)
        assertEquals("ST", staging.initials)
        val local = ShopBrand.forShop("xyz", "Local API (dev)")
        assertEquals(ShopShape.SQUARE, local.shape)
        assertEquals("LO", local.initials)
    }
}
