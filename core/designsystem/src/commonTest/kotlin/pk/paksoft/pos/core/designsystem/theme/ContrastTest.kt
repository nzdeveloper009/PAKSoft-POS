package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

class ContrastTest {

    private fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(c: Color): Double =
        0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)

    private fun ratio(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun assertAa(name: String, fg: Color, bg: Color) {
        val r = ratio(fg, bg)
        assertTrue(r >= 4.5, "$name contrast $r is below 4.5")
    }

    private fun checkScheme(s: ColorScheme) {
        assertAa("primary", s.onPrimary, s.primary)
        assertAa("primaryContainer", s.onPrimaryContainer, s.primaryContainer)
        assertAa("secondary", s.onSecondary, s.secondary)
        assertAa("secondaryContainer", s.onSecondaryContainer, s.secondaryContainer)
        assertAa("tertiary", s.onTertiary, s.tertiary)
        assertAa("tertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer)
        assertAa("error", s.onError, s.error)
        assertAa("errorContainer", s.onErrorContainer, s.errorContainer)
        assertAa("surface", s.onSurface, s.surface)
        assertAa("surface variant text", s.onSurfaceVariant, s.surface)
        assertAa("surfaceContainerLow variant text", s.onSurfaceVariant, s.surfaceContainerLow)
        assertAa("inverseSurface", s.inverseOnSurface, s.inverseSurface)
    }

    @Test
    fun `every light scheme text pair passes AA`() = checkScheme(PosLightColorScheme)

    @Test
    fun `every dark scheme text pair passes AA`() = checkScheme(PosDarkColorScheme)

    @Test
    fun `extended colours pass AA`() {
        listOf(
            PosLightExtendedColors to PosLightColorScheme,
            PosDarkExtendedColors to PosDarkColorScheme,
        ).forEach { (x, s) ->
            assertAa("due chip", x.onDueContainer, x.dueContainer)
            assertAa("paid chip", x.onPaidContainer, x.paidContainer)
            assertAa("due on surface", x.due, s.surface)
        }
    }

    @Test
    fun `every shop brand passes AA in both themes`() {
        listOf("smf", "bsf", "mummyandme", "demo", "staging", "local").forEach { id ->
            val brand = ShopBrand.forShop(id, id)
            listOf(brand.light, brand.dark).forEach { c ->
                assertAa("$id color", c.onColor, c.color)
                assertAa("$id container", c.onContainer, c.container)
            }
        }
    }
}
