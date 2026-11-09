package pk.paksoft.pos.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pk.paksoft.pos.core.model.Money

/** JSON money is a two-decimal string such as "7998.00" (docs/API.md 1.4). */
object MoneySerializer : KSerializer<Money> {
    override val descriptor = PrimitiveSerialDescriptor("pk.paksoft.pos.core.model.Money", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Money) = encoder.encodeString(value.toPlainString())

    override fun deserialize(decoder: Decoder): Money = Money.parse(decoder.decodeString())
}
