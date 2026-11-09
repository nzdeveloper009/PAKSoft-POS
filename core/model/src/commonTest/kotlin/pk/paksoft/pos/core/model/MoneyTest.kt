package pk.paksoft.pos.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test fun `parses two decimal strings`() { assertEquals(Money(799800), Money.parse("7998.00")) }

    @Test fun `parses whole and one decimal`() {
        assertEquals(Money(799800), Money.parse("7998"))
        assertEquals(Money(550), Money.parse("5.5"))
    }

    @Test fun `parses negatives`() { assertEquals(Money(-50), Money.parse("-0.50")) }

    @Test fun `rejects junk`() {
        listOf("", "1,000.00", "1.234", "abc", "1.").forEach {
            assertFailsWith<IllegalArgumentException> { Money.parse(it) }
        }
    }

    @Test fun `formats plain`() {
        assertEquals("7998.00", Money(799800).toPlainString())
        assertEquals("-0.50", Money(-50).toPlainString())
        assertEquals("0.00", Money.ZERO.toPlainString())
    }

    @Test fun `adds and subtracts`() {
        assertEquals(Money(150), Money(100) + Money(50))
        assertEquals(Money(50), Money(100) - Money(50))
    }
}
