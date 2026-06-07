package com.bking.domain.service

import com.bking.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {
    @Test
    fun `parses yuan amount to cents`() {
        assertEquals(Money.cnyCents(1234), AmountParser.parseCny("12.34"))
    }

    @Test
    fun `parses whole yuan amount`() {
        assertEquals(Money.cnyCents(1200), AmountParser.parseCny("12"))
    }

    @Test
    fun `rejects zero blank and negative amounts`() {
        assertNull(AmountParser.parseCny(""))
        assertNull(AmountParser.parseCny("0"))
        assertNull(AmountParser.parseCny("-1"))
    }
}

