package com.maazsurti.slate.core.domain.model

/**
 * Money is held in minor units so totals never drift through floating point.
 * Shop prices here are in whole currency units, so [of] takes a major amount.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money) = Money(cents + other.cents)

    operator fun minus(other: Money) = Money(cents - other.cents)

    operator fun times(quantity: Int) = Money(cents * quantity)

    val isZero: Boolean get() = cents == 0L

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    /** 1,250.75 - grouped, always two decimals. */
    fun format(): String {
        val negative = cents < 0
        val absolute = if (negative) -cents else cents
        val major = absolute / 100
        val minor = absolute % 100
        val grouped = major.toString().reversed().chunked(3).joinToString(",").reversed()
        return buildString {
            if (negative) append('-')
            append(grouped)
            append('.')
            append(minor.toString().padStart(2, '0'))
        }
    }

    companion object {
        val Zero = Money(0)

        fun of(major: Long) = Money(major * 100)

        fun parse(text: String): Money? {
            val cleaned = text.trim().replace(",", "")
            if (cleaned.isEmpty()) return null
            val parts = cleaned.split('.')
            if (parts.size > 2) return null
            val major = parts[0].toLongOrNull() ?: return null
            val minorText = parts.getOrNull(1).orEmpty()
            if (minorText.length > 2 || minorText.any { !it.isDigit() }) return null
            val minor = if (minorText.isEmpty()) 0L else minorText.padEnd(2, '0').toLong()
            val negative = cleaned.startsWith('-')
            val value = major * 100 + minor
            return Money(if (negative) -value else value)
        }
    }
}
