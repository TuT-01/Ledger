package com.bking.domain.service

import com.bking.domain.model.AccountGroup
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money

data class BalanceAccount(
    val accountId: String,
    val group: AccountGroup
) {
    init {
        require(accountId.isNotBlank()) { "Account id cannot be blank." }
    }
}

data class BalanceEntry(
    val accountId: String,
    val direction: EntryDirection,
    val amount: Money
) {
    init {
        require(accountId.isNotBlank()) { "Account id cannot be blank." }
        require(amount.isPositive()) { "Entry amount must be positive." }
    }
}

object AccountBalanceCalculator {
    fun calculate(
        accounts: List<BalanceAccount>,
        entries: List<BalanceEntry>
    ): Map<String, Money> {
        val accountGroups = accounts.associate { it.accountId to it.group }
        val totals = accounts.associate { it.accountId to 0L }.toMutableMap()
        val currencyByAccount = mutableMapOf<String, String>()

        entries.forEach { entry ->
            val group = accountGroups[entry.accountId] ?: return@forEach
            val signedAmount = signedAmountFor(group, entry)
            totals[entry.accountId] = totals.getValue(entry.accountId) + signedAmount
            currencyByAccount.putIfAbsent(entry.accountId, entry.amount.currencyCode)
        }

        return totals.mapValues { (accountId, minorUnits) ->
            val currency = currencyByAccount[accountId] ?: "CNY"
            Money(minorUnits, currency)
        }
    }

    private fun signedAmountFor(
        group: AccountGroup,
        entry: BalanceEntry
    ): Long {
        val debitNormal = when (group) {
            AccountGroup.ASSET,
            AccountGroup.EXPENSE -> true
            AccountGroup.LIABILITY,
            AccountGroup.INCOME,
            AccountGroup.EQUITY -> false
        }

        val increases = if (debitNormal) {
            entry.direction == EntryDirection.DEBIT
        } else {
            entry.direction == EntryDirection.CREDIT
        }

        return if (increases) entry.amount.minorUnits else -entry.amount.minorUnits
    }
}
