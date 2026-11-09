package pk.paksoft.pos.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.Serializable
import pk.paksoft.pos.core.model.Money

class MoneySerializerTest {
    @Serializable
    data class Box(@Serializable(with = MoneySerializer::class) val m: Money?)

    @Test
    fun `round trip`() {
        assertEquals("""{"m":"7998.00"}""", PosJson.encodeToString(Box.serializer(), Box(Money(799800))))
        assertEquals(Money(5), PosJson.decodeFromString(Box.serializer(), """{"m":"0.05"}""").m)
    }

    @Test
    fun `null stays null`() {
        assertNull(PosJson.decodeFromString(Box.serializer(), """{"m":null}""").m)
    }
}
