package com.bking.ui.records

import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.TransactionEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordsCalendarMapperTest {
    @Test
    fun `builds calendar day totals categories and details`() {
        val accounts = listOf(
            account("cash", "现金钱包", AccountGroup.ASSET, AccountType.CASH),
            account("meal-expense", "餐食", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY),
            account("shopping-expense", "Shopping", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY),
            account("salary-income", "工资收入", AccountGroup.INCOME, AccountType.INCOME_CATEGORY)
        )
        val transactions = listOf(
            transaction("meal", TransactionType.EXPENSE, "午饭", "2026-05-13T04:00:00Z"),
            transaction("shopping", TransactionType.EXPENSE, "文具", "2026-05-13T10:30:00Z"),
            transaction("salary", TransactionType.INCOME, "五月工资", "2026-05-13T01:00:00Z")
        )
        val entries = listOf(
            entry("meal:1", "meal", "meal-expense", EntryDirection.DEBIT, 3800),
            entry("meal:2", "meal", "cash", EntryDirection.CREDIT, 3800),
            entry("shopping:1", "shopping", "shopping-expense", EntryDirection.DEBIT, 9000),
            entry("shopping:2", "shopping", "cash", EntryDirection.CREDIT, 9000),
            entry("salary:1", "salary", "cash", EntryDirection.DEBIT, 500000),
            entry("salary:2", "salary", "salary-income", EntryDirection.CREDIT, 500000)
        )

        val state = RecordsCalendarMapper.build(
            visibleMonth = YearMonth.of(2026, 5),
            selectedDate = LocalDate.of(2026, 5, 13),
            transactions = transactions,
            entries = entries,
            accounts = accounts
        )

        val selectedDay = state.days.single { it.date == "2026-05-13" }
        assertTrue(selectedDay.isSelected)
        assertEquals("-¥128", selectedDay.expenseLabel)
        assertEquals("+¥5k", selectedDay.incomeLabel)
        assertEquals("餐食 ¥38.00 · 购物 ¥90.00", selectedDay.categorySummary)

        assertEquals("2026年05月", state.monthLabel)
        assertEquals("05月13日", state.selectedDayTitle)
        assertEquals("收入 +¥5,000.00", state.selectedIncomeLabel)
        assertEquals("支出 -¥128.00", state.selectedExpenseLabel)
        assertEquals(listOf("购物", "餐食", "工资收入"), state.selectedDetails.map { it.category })
        assertEquals("90", state.selectedDetails.first().amountInput)
        assertEquals(RecordEditType.EXPENSE, state.selectedDetails.first().editType)
    }

    private fun account(
        id: String,
        name: String,
        group: AccountGroup,
        type: AccountType
    ): AccountEntity = AccountEntity(
        id = id,
        name = name,
        group = group,
        type = type,
        currencyCode = "CNY",
        openingBalanceMinorUnits = 0,
        isArchived = false,
        createdAt = Instant.parse("2026-05-01T00:00:00Z")
    )

    private fun transaction(
        id: String,
        type: TransactionType,
        note: String,
        occurredAt: String
    ): TransactionEntity = TransactionEntity(
        id = id,
        type = type,
        occurredAt = Instant.parse(occurredAt),
        note = note,
        createdAt = Instant.parse(occurredAt),
        updatedAt = Instant.parse(occurredAt)
    )

    private fun entry(
        id: String,
        transactionId: String,
        accountId: String,
        direction: EntryDirection,
        amountMinorUnits: Long
    ): LedgerEntryEntity = LedgerEntryEntity(
        id = id,
        transactionId = transactionId,
        accountId = accountId,
        direction = direction,
        amountMinorUnits = amountMinorUnits,
        currencyCode = "CNY"
    )
}
