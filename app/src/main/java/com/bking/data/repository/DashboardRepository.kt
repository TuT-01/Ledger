package com.bking.data.repository

import com.bking.data.local.BkingDatabase
import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import com.bking.domain.service.AccountBalanceCalculator
import com.bking.domain.service.BalanceAccount
import com.bking.domain.service.BalanceEntry
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class DashboardRepository @Inject constructor(
    private val database: BkingDatabase
) {
    fun observeSummary(
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Flow<DashboardSummary> {
        val month = YearMonth.now(zoneId)
        val monthStart = month.atDay(1).atStartOfDay(zoneId).toInstant()
        val nextMonthStart = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant()

        return combine(
            database.accountDao().observeActiveAccounts(),
            database.ledgerEntryDao().observeAll()
        ) { accounts, entries ->
            calculateSummary(accounts, entries, monthStart, nextMonthStart)
        }
    }

    companion object {
        fun calculateSummary(
            accounts: List<AccountEntity>,
            entries: List<LedgerEntryEntity>,
            monthStart: Instant,
            nextMonthStart: Instant
        ): DashboardSummary {
            require(monthStart < nextMonthStart) { "Month start must be before next month start." }

            val accountGroups = accounts.associate { it.id to it.group }
            val balances = AccountBalanceCalculator.calculate(
                accounts = accounts.map { BalanceAccount(it.id, it.group) },
                entries = entries.map {
                    BalanceEntry(
                        accountId = it.accountId,
                        direction = it.direction,
                        amount = Money(it.amountMinorUnits, it.currencyCode)
                    )
                }
            )

            val totalAssets = balances.sumForGroup(accounts, AccountGroup.ASSET)
            val totalLiabilities = balances.sumForGroup(accounts, AccountGroup.LIABILITY)
            val monthlyIncome = entries.sumByNormalSide(accountGroups, AccountGroup.INCOME)
            val monthlyExpense = entries.sumByNormalSide(accountGroups, AccountGroup.EXPENSE)

            return DashboardSummary(
                totalAssets = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth = totalAssets - totalLiabilities,
                monthlyIncome = monthlyIncome,
                monthlyExpense = monthlyExpense,
                monthlySurplus = monthlyIncome - monthlyExpense
            )
        }

        private fun Map<String, Money>.sumForGroup(
            accounts: List<AccountEntity>,
            group: AccountGroup
        ): Money {
            val cents = accounts
                .filter { it.group == group }
                .sumOf { get(it.id)?.minorUnits ?: 0L }
            return Money.cnyCents(cents)
        }

        private fun List<LedgerEntryEntity>.sumByNormalSide(
            accountGroups: Map<String, AccountGroup>,
            targetGroup: AccountGroup
        ): Money {
            val cents = filter { accountGroups[it.accountId] == targetGroup }
                .sumOf { entry ->
                    val normalDirection = when (targetGroup) {
                        AccountGroup.INCOME,
                        AccountGroup.LIABILITY,
                        AccountGroup.EQUITY -> EntryDirection.CREDIT
                        AccountGroup.ASSET,
                        AccountGroup.EXPENSE -> EntryDirection.DEBIT
                    }
                    if (entry.direction == normalDirection) {
                        entry.amountMinorUnits
                    } else {
                        -entry.amountMinorUnits
                    }
                }
            return Money.cnyCents(cents.coerceAtLeast(0))
        }

    }
}

data class DashboardSummary(
    val totalAssets: Money = Money.cnyCents(0),
    val totalLiabilities: Money = Money.cnyCents(0),
    val netWorth: Money = Money.cnyCents(0),
    val monthlyIncome: Money = Money.cnyCents(0),
    val monthlyExpense: Money = Money.cnyCents(0),
    val monthlySurplus: Money = Money.cnyCents(0)
)
