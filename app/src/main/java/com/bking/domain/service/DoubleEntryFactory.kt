package com.bking.domain.service

import com.bking.domain.model.EntryDirection
import com.bking.domain.model.LedgerEntryDraft
import com.bking.domain.model.Money
import com.bking.domain.model.TransactionDraft
import com.bking.domain.model.TransactionType
import java.time.Instant

object DoubleEntryFactory {
    fun expense(
        amount: Money,
        paidFromAccountId: String,
        expenseAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.EXPENSE,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = expenseAccountId,
        creditAccountId = paidFromAccountId,
        amount = amount
    )

    fun income(
        amount: Money,
        receivedToAccountId: String,
        incomeAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.INCOME,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = receivedToAccountId,
        creditAccountId = incomeAccountId,
        amount = amount
    )

    fun transfer(
        amount: Money,
        fromAccountId: String,
        toAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.TRANSFER,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = toAccountId,
        creditAccountId = fromAccountId,
        amount = amount
    )

    fun creditCardExpense(
        amount: Money,
        creditCardAccountId: String,
        expenseAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.CREDIT_CARD_EXPENSE,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = expenseAccountId,
        creditAccountId = creditCardAccountId,
        amount = amount
    )

    fun repayLiability(
        amount: Money,
        paidFromAccountId: String,
        liabilityAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.LIABILITY_REPAYMENT,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = liabilityAccountId,
        creditAccountId = paidFromAccountId,
        amount = amount
    )

    fun lend(
        amount: Money,
        paidFromAccountId: String,
        receivableAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.LEND,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = receivableAccountId,
        creditAccountId = paidFromAccountId,
        amount = amount
    )

    fun borrow(
        amount: Money,
        receivedToAccountId: String,
        liabilityAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.BORROW,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = receivedToAccountId,
        creditAccountId = liabilityAccountId,
        amount = amount
    )

    fun loanPayment(
        principal: Money,
        interest: Money,
        paidFromAccountId: String,
        liabilityAccountId: String,
        interestExpenseAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft {
        require(principal.isPositive()) { "Principal amount must be positive." }
        require(interest.minorUnits >= 0) { "Interest amount cannot be negative." }
        require(principal.currencyCode == interest.currencyCode) { "Currency mismatch." }
        val total = principal + interest
        val entries = buildList {
            add(LedgerEntryDraft(liabilityAccountId, EntryDirection.DEBIT, principal))
            if (interest.minorUnits > 0) {
                add(LedgerEntryDraft(interestExpenseAccountId, EntryDirection.DEBIT, interest))
            }
            add(LedgerEntryDraft(paidFromAccountId, EntryDirection.CREDIT, total))
        }
        return TransactionDraft(
            type = TransactionType.LOAN_PAYMENT,
            occurredAt = occurredAt,
            note = note.trim(),
            entries = entries
        )
    }

    fun investmentBuy(
        amount: Money,
        paidFromAccountId: String,
        investmentAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.INVESTMENT_BUY,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = investmentAccountId,
        creditAccountId = paidFromAccountId,
        amount = amount
    )

    fun investmentSell(
        amount: Money,
        receivedToAccountId: String,
        investmentAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.INVESTMENT_SELL,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = receivedToAccountId,
        creditAccountId = investmentAccountId,
        amount = amount
    )

    fun openingAssetBalance(
        amount: Money,
        assetAccountId: String,
        equityAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.BALANCE_ADJUSTMENT,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = assetAccountId,
        creditAccountId = equityAccountId,
        amount = amount
    )

    fun openingLiabilityBalance(
        amount: Money,
        liabilityAccountId: String,
        equityAccountId: String,
        occurredAt: Instant,
        note: String
    ): TransactionDraft = transaction(
        type = TransactionType.BALANCE_ADJUSTMENT,
        occurredAt = occurredAt,
        note = note,
        debitAccountId = equityAccountId,
        creditAccountId = liabilityAccountId,
        amount = amount
    )

    private fun transaction(
        type: TransactionType,
        occurredAt: Instant,
        note: String,
        debitAccountId: String,
        creditAccountId: String,
        amount: Money
    ): TransactionDraft = TransactionDraft(
        type = type,
        occurredAt = occurredAt,
        note = note.trim(),
        entries = listOf(
            LedgerEntryDraft(debitAccountId, EntryDirection.DEBIT, amount),
            LedgerEntryDraft(creditAccountId, EntryDirection.CREDIT, amount)
        )
    )
}
