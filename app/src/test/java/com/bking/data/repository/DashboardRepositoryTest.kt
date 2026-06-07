package com.bking.data.repository

import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardRepositoryTest {
    @Test
    fun `summary calculates assets liabilities net worth and monthly cash flow`() {
        val now = Instant.parse("2026-05-12T10:00:00Z")
        val monthStart = Instant.parse("2026-05-01T00:00:00Z")
        val nextMonthStart = Instant.parse("2026-06-01T00:00:00Z")
        val accounts = listOf(
            account("bank", AccountGroup.ASSET, AccountType.BANK, now),
            account("credit-card", AccountGroup.LIABILITY, AccountType.CREDIT_CARD, now),
            account("salary", AccountGroup.INCOME, AccountType.INCOME_CATEGORY, now),
            account("food", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY, now)
        )
        val entries = listOf(
            entry("1", "bank", EntryDirection.DEBIT, 100000),
            entry("2", "credit-card", EntryDirection.CREDIT, 30000),
            entry("3", "salary", EntryDirection.CREDIT, 800000),
            entry("4", "food", EntryDirection.DEBIT, 12000)
        )

        val summary = DashboardRepository.calculateSummary(
            accounts = accounts,
            entries = entries,
            monthStart = monthStart,
            nextMonthStart = nextMonthStart
        )

        assertEquals(Money.cnyCents(100000), summary.totalAssets)
        assertEquals(Money.cnyCents(30000), summary.totalLiabilities)
        assertEquals(Money.cnyCents(70000), summary.netWorth)
        assertEquals(Money.cnyCents(800000), summary.monthlyIncome)
        assertEquals(Money.cnyCents(12000), summary.monthlyExpense)
        assertEquals(Money.cnyCents(788000), summary.monthlySurplus)
    }

    @Test
    fun `summary keeps negative balance and surplus when expenses exceed available money`() {
        val now = Instant.parse("2026-05-12T10:00:00Z")
        val monthStart = Instant.parse("2026-05-01T00:00:00Z")
        val nextMonthStart = Instant.parse("2026-06-01T00:00:00Z")
        val accounts = listOf(
            account("bank", AccountGroup.ASSET, AccountType.BANK, now),
            account("food", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY, now)
        )
        val entries = listOf(
            entry("1", "bank", EntryDirection.CREDIT, 12000),
            entry("2", "food", EntryDirection.DEBIT, 12000)
        )

        val summary = DashboardRepository.calculateSummary(
            accounts = accounts,
            entries = entries,
            monthStart = monthStart,
            nextMonthStart = nextMonthStart
        )

        assertEquals(-12000L, summary.totalAssets.minorUnits)
        assertEquals(-12000L, summary.netWorth.minorUnits)
        assertEquals(Money.cnyCents(12000), summary.monthlyExpense)
        assertEquals(-12000L, summary.monthlySurplus.minorUnits)
    }

    private fun account(
        id: String,
        group: AccountGroup,
        type: AccountType,
        createdAt: Instant
    ): AccountEntity = AccountEntity(
        id = id,
        name = id,
        group = group,
        type = type,
        currencyCode = "CNY",
        openingBalanceMinorUnits = 0,
        isArchived = false,
        createdAt = createdAt
    )

    private fun entry(
        id: String,
        accountId: String,
        direction: EntryDirection,
        amount: Long
    ): LedgerEntryEntity = LedgerEntryEntity(
        id = id,
        transactionId = "txn-$id",
        accountId = accountId,
        direction = direction,
        amountMinorUnits = amount,
        currencyCode = "CNY"
    )
}
