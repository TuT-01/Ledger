package com.bking.domain.model

import java.time.Instant

data class LedgerEntryDraft(
    val accountId: String,
    val direction: EntryDirection,
    val amount: Money
) {
    init {
        require(accountId.isNotBlank()) { "Account id cannot be blank." }
        require(amount.isPositive()) { "Entry amount must be positive." }
    }
}

data class TransactionDraft(
    val type: TransactionType,
    val occurredAt: Instant,
    val note: String,
    val entries: List<LedgerEntryDraft>
) {
    init {
        require(entries.size >= 2) { "A double-entry transaction needs at least two entries." }
        require(isBalanced()) { "Debit entries must equal credit entries." }
    }

    fun isBalanced(): Boolean {
        val currency = entries.first().amount.currencyCode
        val debitTotal = entries
            .filter { it.direction == EntryDirection.DEBIT }
            .sumOf { requireCurrency(currency, it.amount).minorUnits }
        val creditTotal = entries
            .filter { it.direction == EntryDirection.CREDIT }
            .sumOf { requireCurrency(currency, it.amount).minorUnits }

        return debitTotal == creditTotal
    }

    private fun requireCurrency(expected: String, money: Money): Money {
        require(money.currencyCode == expected) {
            "All entries in one transaction must use the same currency."
        }
        return money
    }
}

