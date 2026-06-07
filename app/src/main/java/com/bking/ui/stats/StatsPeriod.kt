package com.bking.ui.stats

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

data class StatsRange(
    val start: Instant,
    val endExclusive: Instant,
    val startLabel: String,
    val endLabel: String
)

object StatsPeriod {
    fun week(
        input: String,
        zoneId: ZoneId = ZoneId.of("Asia/Shanghai")
    ): StatsRange? = try {
        val startDate = LocalDate.parse(input.trim())
        val endDate = startDate.plusWeeks(1)
        StatsRange(
            start = startDate.atStartOfDay(zoneId).toInstant(),
            endExclusive = endDate.atStartOfDay(zoneId).toInstant(),
            startLabel = startDate.toString(),
            endLabel = endDate.toString()
        )
    } catch (_: DateTimeParseException) {
        null
    }

    fun month(
        input: String,
        zoneId: ZoneId = ZoneId.of("Asia/Shanghai")
    ): StatsRange? = try {
        val startDate = LocalDate.parse(input.trim())
        val endDate = startDate.plusMonths(1)
        StatsRange(
            start = startDate.atStartOfDay(zoneId).toInstant(),
            endExclusive = endDate.atStartOfDay(zoneId).toInstant(),
            startLabel = startDate.toString(),
            endLabel = endDate.toString()
        )
    } catch (_: DateTimeParseException) {
        null
    }

    fun year(
        input: String,
        zoneId: ZoneId = ZoneId.of("Asia/Shanghai")
    ): StatsRange? = try {
        val startDate = LocalDate.parse(input.trim())
        val endDate = startDate.plusYears(1)
        StatsRange(
            start = startDate.atStartOfDay(zoneId).toInstant(),
            endExclusive = endDate.atStartOfDay(zoneId).toInstant(),
            startLabel = startDate.toString(),
            endLabel = endDate.toString()
        )
    } catch (_: DateTimeParseException) {
        null
    }
}
