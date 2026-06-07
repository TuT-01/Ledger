package com.bking.data.repository

import com.bking.data.local.BkingDatabase
import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.model.LedgerEntryWithTransaction
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class StatsRepository @Inject constructor(
    private val database: BkingDatabase
) {
    fun observeSummary(
        start: Instant,
        endExclusive: Instant
    ): Flow<StatsSummary> = combine(
        database.accountDao().observeActiveAccounts(),
        database.ledgerEntryDao().observeEntriesBetween(start, endExclusive)
    ) { accounts, entries ->
        calculate(accounts, entries)
    }

    companion object {
        fun calculate(
            accounts: List<AccountEntity>,
            entries: List<LedgerEntryWithTransaction>
        ): StatsSummary {
            val groups = accounts.associate { it.id to it.group }
            val accountsById = accounts.associateBy { it.id }
            val income = entries.sumNormal(groups, AccountGroup.INCOME)
            val expense = entries.sumNormal(groups, AccountGroup.EXPENSE)

            return StatsSummary(
                income = income,
                expense = expense,
                surplus = income.minusFloor(expense),
                incomeBreakdown = entries.breakdown(accountsById, AccountGroup.INCOME),
                expenseBreakdown = entries.breakdown(accountsById, AccountGroup.EXPENSE)
            )
        }

        private fun List<LedgerEntryWithTransaction>.sumNormal(
            accountGroups: Map<String, AccountGroup>,
            group: AccountGroup
        ): Money {
            val normal = when (group) {
                AccountGroup.INCOME,
                AccountGroup.LIABILITY,
                AccountGroup.EQUITY -> EntryDirection.CREDIT
                AccountGroup.ASSET,
                AccountGroup.EXPENSE -> EntryDirection.DEBIT
            }
            val cents = filter { accountGroups[it.accountId] == group }
                .sumOf { if (it.direction == normal) it.amountMinorUnits else -it.amountMinorUnits }
                .coerceAtLeast(0)
            return Money.cnyCents(cents)
        }

        private fun List<LedgerEntryWithTransaction>.breakdown(
            accountsById: Map<String, AccountEntity>,
            group: AccountGroup
        ): List<CategoryBreakdown> {
            val normal = when (group) {
                AccountGroup.INCOME,
                AccountGroup.LIABILITY,
                AccountGroup.EQUITY -> EntryDirection.CREDIT
                AccountGroup.ASSET,
                AccountGroup.EXPENSE -> EntryDirection.DEBIT
            }

            return filter { accountsById[it.accountId]?.group == group }
                .groupBy { it.accountId }
                .mapNotNull { (accountId, accountEntries) ->
                    val cents = accountEntries
                        .sumOf { if (it.direction == normal) it.amountMinorUnits else -it.amountMinorUnits }
                        .coerceAtLeast(0)
                    val account = accountsById[accountId]
                    if (account == null || cents <= 0) {
                        null
                    } else {
                        CategoryBreakdown(
                            accountId = account.id,
                            label = account.name,
                            amount = Money.cnyCents(cents)
                        )
                    }
                }
                .sortedByDescending { it.amount.minorUnits }
        }

        private fun Money.minusFloor(other: Money): Money {
            require(currencyCode == other.currencyCode) { "Currency mismatch." }
            return Money((minorUnits - other.minorUnits).coerceAtLeast(0), currencyCode)
        }
    }
}

data class StatsSummary(
    val income: Money = Money.cnyCents(0),
    val expense: Money = Money.cnyCents(0),
    val surplus: Money = Money.cnyCents(0),
    val incomeBreakdown: List<CategoryBreakdown> = emptyList(),
    val expenseBreakdown: List<CategoryBreakdown> = emptyList()
)

data class CategoryBreakdown(
    val accountId: String,
    val label: String,
    val amount: Money
)
