package com.bking.data.repository

import com.bking.data.local.model.LedgerEntryWithTransaction
import com.bking.data.local.entity.AccountEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsRepositoryTest {
    @Test
    fun `stats summary calculates income expense and surplus`() {
        val createdAt = Instant.parse("2026-05-12T10:00:00Z")
        val accounts = listOf(
            account("salary", "Salary", AccountGroup.INCOME, AccountType.INCOME_CATEGORY, createdAt),
            account("food", "Food", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY, createdAt)
        )
        val entries = listOf(
            entry("1", "salary", EntryDirection.CREDIT, 880000, createdAt),
            entry("2", "food", EntryDirection.DEBIT, 1234, createdAt)
        )

        val summary = StatsRepository.calculate(accounts, entries)

        assertEquals(Money.cnyCents(880000), summary.income)
        assertEquals(Money.cnyCents(1234), summary.expense)
        assertEquals(Money.cnyCents(878766), summary.surplus)
    }

    @Test
    fun `stats summary calculates category breakdowns by group`() {
        val createdAt = Instant.parse("2026-05-12T10:00:00Z")
        val accounts = listOf(
            account("salary", "Salary", AccountGroup.INCOME, AccountType.INCOME_CATEGORY, createdAt),
            account("external", "External", AccountGroup.INCOME, AccountType.INCOME_CATEGORY, createdAt),
            account("meal", "Meal", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY, createdAt),
            account("sport", "Sport", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY, createdAt)
        )
        val entries = listOf(
            entry("1", "salary", EntryDirection.CREDIT, 800000, createdAt),
            entry("2", "external", EntryDirection.CREDIT, 120000, createdAt),
            entry("3", "meal", EntryDirection.DEBIT, 5000, createdAt),
            entry("4", "sport", EntryDirection.DEBIT, 3000, createdAt),
            entry("5", "meal", EntryDirection.DEBIT, 2000, createdAt)
        )

        val summary = StatsRepository.calculate(accounts, entries)

        assertEquals(
            listOf(
                CategoryBreakdown("salary", "Salary", Money.cnyCents(800000)),
                CategoryBreakdown("external", "External", Money.cnyCents(120000))
            ),
            summary.incomeBreakdown
        )
        assertEquals(
            listOf(
                CategoryBreakdown("meal", "Meal", Money.cnyCents(7000)),
                CategoryBreakdown("sport", "Sport", Money.cnyCents(3000))
            ),
            summary.expenseBreakdown
        )
    }

    private fun account(
        id: String,
        name: String,
        group: AccountGroup,
        type: AccountType,
        createdAt: Instant
    ) = AccountEntity(
        id = id,
        name = name,
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
        amount: Long,
        occurredAt: Instant
    ) = LedgerEntryWithTransaction(
        id = id,
        transactionId = "txn-$id",
        accountId = accountId,
        direction = direction,
        amountMinorUnits = amount,
        currencyCode = "CNY",
        transactionOccurredAt = occurredAt
    )
}
