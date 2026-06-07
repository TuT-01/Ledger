package com.bking.domain.service

import com.bking.domain.model.AccountGroup
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountBalanceCalculatorTest {
    @Test
    fun `asset balance increases with debit and decreases with credit`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("bank", AccountGroup.ASSET)),
            entries = listOf(
                BalanceEntry("bank", EntryDirection.DEBIT, Money.cnyCents(100000)),
                BalanceEntry("bank", EntryDirection.CREDIT, Money.cnyCents(2350))
            )
        )

        assertEquals(Money.cnyCents(97650), balances.getValue("bank"))
    }

    @Test
    fun `asset balance can go negative when credits exceed debits`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("bank", AccountGroup.ASSET)),
            entries = listOf(
                BalanceEntry("bank", EntryDirection.CREDIT, Money.cnyCents(2350))
            )
        )

        assertEquals(-2350L, balances.getValue("bank").minorUnits)
    }

    @Test
    fun `liability balance increases with credit and decreases with debit`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("credit-card", AccountGroup.LIABILITY)),
            entries = listOf(
                BalanceEntry("credit-card", EntryDirection.CREDIT, Money.cnyCents(30000)),
                BalanceEntry("credit-card", EntryDirection.DEBIT, Money.cnyCents(12000))
            )
        )

        assertEquals(Money.cnyCents(18000), balances.getValue("credit-card"))
    }

    @Test
    fun `expense balance follows debit normal side`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("food", AccountGroup.EXPENSE)),
            entries = listOf(
                BalanceEntry("food", EntryDirection.DEBIT, Money.cnyCents(1800)),
                BalanceEntry("food", EntryDirection.DEBIT, Money.cnyCents(2200))
            )
        )

        assertEquals(Money.cnyCents(4000), balances.getValue("food"))
    }

    @Test
    fun `income balance follows credit normal side`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("salary", AccountGroup.INCOME)),
            entries = listOf(
                BalanceEntry("salary", EntryDirection.CREDIT, Money.cnyCents(880000)),
                BalanceEntry("salary", EntryDirection.DEBIT, Money.cnyCents(10000))
            )
        )

        assertEquals(Money.cnyCents(870000), balances.getValue("salary"))
    }

    @Test
    fun `missing account entries are ignored`() {
        val balances = AccountBalanceCalculator.calculate(
            accounts = listOf(BalanceAccount("bank", AccountGroup.ASSET)),
            entries = listOf(
                BalanceEntry("bank", EntryDirection.DEBIT, Money.cnyCents(1000)),
                BalanceEntry("unknown", EntryDirection.DEBIT, Money.cnyCents(999999))
            )
        )

        assertEquals(mapOf("bank" to Money.cnyCents(1000)), balances)
    }
}
