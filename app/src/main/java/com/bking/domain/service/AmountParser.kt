package com.bking.domain.service

import com.bking.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

object AmountParser {
    fun parseCny(input: String): Money? {
        val normalized = input.trim()
        if (normalized.isBlank()) {
            return null
        }

        val yuan = normalized.toBigDecimalOrNull() ?: return null
        if (yuan <= BigDecimal.ZERO) {
            return null
        }

        val cents = yuan
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()

        return Money.cnyCents(cents)
    }
}

