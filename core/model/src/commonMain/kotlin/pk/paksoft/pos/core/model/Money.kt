package pk.paksoft.pos.core.model

import kotlin.jvm.JvmInline

/** An amount in paisa (1/100 rupee). Never use Double or Float for money. */
@JvmInline
value class Money(val paisa: Long) : Comparable<Money> {
    operator fun plus(other: Money) = Money(paisa + other.paisa)
    operator fun minus(other: Money) = Money(paisa - other.paisa)
    override fun compareTo(other: Money) = paisa.compareTo(other.paisa)

    /** "7998.00" or "-0.50": always two decimals, no grouping or currency symbol. */
    fun toPlainString(): String {
        val abs = if (paisa < 0) -paisa else paisa
        val sign = if (paisa < 0) "-" else ""
        return "$sign${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    }

    companion object {
        val ZERO = Money(0)
        private val pattern = Regex("""(-?)(\d+)(?:\.(\d{1,2}))?""")

        /** Accepts `-?\d+(\.\d{1,2})?`; anything else throws [IllegalArgumentException]. */
        fun parse(text: String): Money {
            val match = requireNotNull(pattern.matchEntire(text)) { "Not a money amount: \"$text\"" }
            val (sign, whole, fraction) = match.destructured
            val paisa = whole.toLong() * 100 + fraction.padEnd(2, '0').toLong()
            return Money(if (sign.isEmpty()) paisa else -paisa)
        }
    }
}
