package com.bking.data.repository

import com.bking.domain.model.Money
import com.bking.domain.service.DoubleEntryFactory
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val accountSeedRepository: AccountSeedRepository
) {
    suspend fun addExpense(
        amount: Money,
        note: String,
        kind: ExpenseKind,
        occurredAt: Instant = Instant.now()
    ) {
        addExpenseToAccount(
            amount = amount,
            note = note,
            expenseAccountId = kind.accountId,
            paidFromAccountId = DefaultAccountIds.CASH,
            occurredAt = occurredAt
        )
    }

    suspend fun addIncome(
        amount: Money,
        note: String,
        kind: IncomeKind,
        occurredAt: Instant = Instant.now()
    ) {
        addIncomeToAccount(
            amount = amount,
            note = note,
            incomeAccountId = kind.accountId,
            receivedToAccountId = DefaultAccountIds.CASH,
            occurredAt = occurredAt
        )
    }

    suspend fun addExpenseToAccount(
        amount: Money,
        note: String,
        expenseAccountId: String,
        paidFromAccountId: String,
        occurredAt: Instant = Instant.now()
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.expense(
            amount = amount,
            paidFromAccountId = paidFromAccountId,
            expenseAccountId = expenseAccountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }

    suspend fun addIncomeToAccount(
        amount: Money,
        note: String,
        incomeAccountId: String,
        receivedToAccountId: String,
        occurredAt: Instant = Instant.now()
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.income(
            amount = amount,
            receivedToAccountId = receivedToAccountId,
            incomeAccountId = incomeAccountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }

    suspend fun addTransfer(
        amount: Money,
        note: String,
        fromAccountId: String,
        toAccountId: String,
        occurredAt: Instant = Instant.now()
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.transfer(
            amount = amount,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }

    suspend fun repayLiability(
        amount: Money,
        note: String,
        paidFromAccountId: String,
        liabilityAccountId: String,
        occurredAt: Instant = Instant.now()
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.repayLiability(
            amount = amount,
            paidFromAccountId = paidFromAccountId,
            liabilityAccountId = liabilityAccountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }
}

object DefaultAccountIds {
    const val CASH = "cash"
    const val BANK = "bank"
    const val CREDIT_CARD = "credit-card"
    const val SALARY_INCOME = "salary-income"
    const val EXTERNAL_INCOME = "external-income"
    const val OTHER_INCOME = "other-income"
    const val MEAL_EXPENSE = "meal-expense"
    const val SHOPPING_EXPENSE = "shopping-expense"
    const val SPORT_EXPENSE = "sport-expense"
    const val LOAN_INTEREST_EXPENSE = "loan-interest-expense"
    const val OTHER_EXPENSE = "other-expense"
}

enum class IncomeKind(val label: String, val icon: String, val accountId: String) {
    SALARY("工资收入", "薪", DefaultAccountIds.SALARY_INCOME),
    EXTERNAL("外部收入", "外", DefaultAccountIds.EXTERNAL_INCOME),
    OTHER("其他收入", "...", DefaultAccountIds.OTHER_INCOME)
}

enum class ExpenseKind(val label: String, val icon: String, val accountId: String) {
    MEAL("餐饮", "餐", DefaultAccountIds.MEAL_EXPENSE),
    SHOPPING("购物", "购", DefaultAccountIds.SHOPPING_EXPENSE),
    SPORT("运动", "动", DefaultAccountIds.SPORT_EXPENSE),
    OTHER("其他支出", "...", DefaultAccountIds.OTHER_EXPENSE)
}
