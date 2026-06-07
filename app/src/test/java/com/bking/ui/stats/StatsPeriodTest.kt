package com.bking.ui.stats

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsPeriodTest {
    @Test
    fun `week period uses start date and next week`() {
        val range = StatsPeriod.week("2026-05-13")

        assertEquals(Instant.parse("2026-05-12T16:00:00Z"), range?.start)
        assertEquals(Instant.parse("2026-05-19T16:00:00Z"), range?.endExclusive)
        assertEquals("2026-05-20", range?.endLabel)
    }

    @Test
    fun `month period uses start date and same day next month`() {
        val range = StatsPeriod.month("2026-05-13")

        assertEquals(Instant.parse("2026-05-12T16:00:00Z"), range?.start)
        assertEquals(Instant.parse("2026-06-12T16:00:00Z"), range?.endExclusive)
        assertEquals("2026-06-13", range?.endLabel)
    }

    @Test
    fun `year period uses start date and same day next year`() {
        val range = StatsPeriod.year("2026-05-13")

        assertEquals(Instant.parse("2026-05-12T16:00:00Z"), range?.start)
        assertEquals(Instant.parse("2027-05-12T16:00:00Z"), range?.endExclusive)
        assertEquals("2027-05-13", range?.endLabel)
    }

    @Test
    fun `invalid input returns null`() {
        assertNull(StatsPeriod.week("2026-13-01"))
        assertNull(StatsPeriod.month("2026-13-01"))
        assertNull(StatsPeriod.year("20x6-01-01"))
    }
}
