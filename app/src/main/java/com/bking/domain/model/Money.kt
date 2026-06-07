package com.bking.domain.model

data class Money(
    val minorUnits: Long,
    val currencyCode: String
) : Comparable<Money> {
    init {
        require(currencyCode.isNotBlank()) { "Currency code cannot be blank." }
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    fun isPositive(): Boolean = minorUnits > 0

    private fun requireSameCurrency(other: Money) {
        require(currencyCode == other.currencyCode) {
            "Currency mismatch: $currencyCode != ${other.currencyCode}."
        }
    }

    companion object {
        fun cnyCents(cents: Long): Money = Money(cents, "CNY")
    }
}
