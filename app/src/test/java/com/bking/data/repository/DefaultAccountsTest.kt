package com.bking.data.repository

import com.bking.domain.model.AccountGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAccountsTest {
    @Test
    fun `default accounts include other categories for income and expense`() {
        val accountsById = DefaultAccounts.items.associateBy { it.id }

        assertEquals(AccountGroup.INCOME, accountsById.getValue(IncomeKind.OTHER.accountId).group)
        assertEquals("其他", accountsById.getValue(IncomeKind.OTHER.accountId).name)
        assertEquals(AccountGroup.EXPENSE, accountsById.getValue(ExpenseKind.OTHER.accountId).group)
        assertEquals("其他", accountsById.getValue(ExpenseKind.OTHER.accountId).name)
    }

    @Test
    fun `income and expense kinds expose low key other category`() {
        assertTrue(IncomeKind.entries.any { it.label == "其他" && it.icon == "..." })
        assertTrue(ExpenseKind.entries.any { it.label == "其他" && it.icon == "..." })
    }
}
