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
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.expense(
            amount = amount,
            paidFromAccountId = DefaultAccountIds.CASH,
            expenseAccountId = kind.accountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }

    suspend fun addIncome(
        amount: Money,
        note: String,
        kind: IncomeKind,
        occurredAt: Instant = Instant.now()
    ) {
        accountSeedRepository.seedDefaultsIfNeeded()
        val draft = DoubleEntryFactory.income(
            amount = amount,
            receivedToAccountId = DefaultAccountIds.CASH,
            incomeAccountId = kind.accountId,
            occurredAt = occurredAt,
            note = note
        )
        ledgerRepository.createTransaction(UUID.randomUUID().toString(), draft)
    }
}

object DefaultAccountIds {
    const val CASH = "cash"
    const val SALARY_INCOME = "salary-income"
    const val EXTERNAL_INCOME = "external-income"
    const val OTHER_INCOME = "other-income"
    const val MEAL_EXPENSE = "meal-expense"
    const val SHOPPING_EXPENSE = "shopping-expense"
    const val SPORT_EXPENSE = "sport-expense"
    const val OTHER_EXPENSE = "other-expense"
}

enum class IncomeKind(val label: String, val icon: String, val accountId: String) {
    SALARY("工资收入", "💼", DefaultAccountIds.SALARY_INCOME),
    EXTERNAL("外部收入", "✨", DefaultAccountIds.EXTERNAL_INCOME),
    OTHER("其他", "...", DefaultAccountIds.OTHER_INCOME)
}

enum class ExpenseKind(val label: String, val icon: String, val accountId: String) {
    MEAL("餐食", "🍽", DefaultAccountIds.MEAL_EXPENSE),
    SHOPPING("购物", "🛒", DefaultAccountIds.SHOPPING_EXPENSE),
    SPORT("运动", "🏃", DefaultAccountIds.SPORT_EXPENSE),
    OTHER("其他", "...", DefaultAccountIds.OTHER_EXPENSE)
}
