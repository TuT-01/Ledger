package com.bking.data.repository

import com.bking.domain.model.Money
import com.bking.domain.service.DoubleEntryFactory
import java.time.Instant
import javax.inject.Inject

class RecordMutationUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val accountSeedRepository: AccountSeedRepository
) {
    suspend fun updateExpense(
        transactionId: String,
        amount: Money,
        note: String,
        kind: ExpenseKind,
        occurredAt: Instant
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        ledgerRepository.replaceTransaction(
            id = transactionId,
            draft = DoubleEntryFactory.expense(
                amount = amount,
                paidFromAccountId = DefaultAccountIds.CASH,
                expenseAccountId = kind.accountId,
                occurredAt = occurredAt,
                note = note
            )
        )
    }

    suspend fun updateIncome(
        transactionId: String,
        amount: Money,
        note: String,
        kind: IncomeKind,
        occurredAt: Instant
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        ledgerRepository.replaceTransaction(
            id = transactionId,
            draft = DoubleEntryFactory.income(
                amount = amount,
                receivedToAccountId = DefaultAccountIds.CASH,
                incomeAccountId = kind.accountId,
                occurredAt = occurredAt,
                note = note
            )
        )
    }

    suspend fun delete(transactionId: String) {
        ledgerRepository.deleteTransaction(transactionId)
    }
}
