package com.bking.ui.records

import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.TransactionEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.TransactionType
import com.bking.data.repository.ExpenseKind
import com.bking.data.repository.IncomeKind
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object RecordsCalendarMapper {
    private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")
    private val dayTitleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

    fun build(
        visibleMonth: YearMonth,
        selectedDate: LocalDate,
        transactions: List<TransactionEntity>,
        entries: List<LedgerEntryEntity>,
        accounts: List<AccountEntity>
    ): RecordsUiState {
        val accountById = accounts.associateBy { it.id }
        val entriesByTransaction = entries.groupBy { it.transactionId }
        val summaries = transactions.mapNotNull { transaction ->
            transaction.toRecordSummary(entriesByTransaction[transaction.id].orEmpty(), accountById)
        }
        val summariesByDate = summaries.groupBy { it.date }
        val selectedSummaries = summariesByDate[selectedDate].orEmpty()

        return RecordsUiState(
            visibleMonth = visibleMonth,
            monthLabel = visibleMonth.format(monthFormatter),
            selectedDate = selectedDate,
            selectedDayTitle = selectedDate.format(dayTitleFormatter),
            days = buildCalendarDays(visibleMonth, selectedDate, summariesByDate),
            selectedIncomeLabel = "收入 ${formatSigned(selectedSummaries.sumOf { it.incomeMinorUnits }, '+')}",
            selectedExpenseLabel = "支出 ${formatSigned(selectedSummaries.sumOf { it.expenseMinorUnits }, '-')}",
            selectedCategorySummary = selectedSummaries.categorySummary(),
            selectedDetails = selectedSummaries
                .sortedByDescending { it.occurredAtEpochMillis }
                .map { it.toDetailUiState() }
        )
    }

    private fun buildCalendarDays(
        visibleMonth: YearMonth,
        selectedDate: LocalDate,
        summariesByDate: Map<LocalDate, List<RecordSummary>>
    ): List<RecordCalendarDayUiState> {
        val firstDay = visibleMonth.atDay(1)
        val leadingDays = firstDay.dayOfWeek.value - 1
        val startDate = firstDay.minusDays(leadingDays.toLong())
        val cellCount = ((leadingDays + visibleMonth.lengthOfMonth() + 6) / 7) * 7

        return (0 until cellCount).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val summaries = summariesByDate[date].orEmpty()
            val income = summaries.sumOf { it.incomeMinorUnits }
            val expense = summaries.sumOf { it.expenseMinorUnits }
            RecordCalendarDayUiState(
                date = date.toString(),
                dayOfMonth = date.dayOfMonth.toString(),
                isInVisibleMonth = YearMonth.from(date) == visibleMonth,
                isSelected = date == selectedDate,
                hasRecords = summaries.isNotEmpty(),
                incomeLabel = if (income > 0) formatCalendarSigned(income, '+') else "",
                expenseLabel = if (expense > 0) formatCalendarSigned(expense, '-') else "",
                categorySummary = summaries.categorySummary()
            )
        }
    }

    private fun TransactionEntity.toRecordSummary(
        entries: List<LedgerEntryEntity>,
        accountById: Map<String, AccountEntity>
    ): RecordSummary? {
        val categoryEntries = entries.mapNotNull { entry ->
            val account = accountById[entry.accountId] ?: return@mapNotNull null
            val normalDirection = account.group.normalDirection() ?: return@mapNotNull null
            if (entry.direction != normalDirection) {
                return@mapNotNull null
            }
            RecordCategoryAmount(
                accountId = account.id,
                category = account.displayName(),
                group = account.group,
                amountMinorUnits = entry.amountMinorUnits
            )
        }

        if (categoryEntries.isEmpty()) {
            return null
        }

        val income = categoryEntries
            .filter { it.group == AccountGroup.INCOME }
            .sumOf { it.amountMinorUnits }
        val expense = categoryEntries
            .filter { it.group == AccountGroup.EXPENSE }
            .sumOf { it.amountMinorUnits }
        val primaryCategory = categoryEntries.firstOrNull { it.group == AccountGroup.EXPENSE }
            ?: categoryEntries.first()

        return RecordSummary(
            id = id,
            type = type.displayName(),
            note = note.ifBlank { "无备注" },
            date = occurredAt.atZone(zoneId).toLocalDate(),
            time = occurredAt.atZone(zoneId).format(timeFormatter),
            occurredAtEpochMillis = occurredAt.toEpochMilli(),
            category = primaryCategory.category,
            incomeMinorUnits = income,
            expenseMinorUnits = expense,
            categoryAmounts = categoryEntries
        )
    }

    private fun List<RecordSummary>.categorySummary(): String {
        return flatMap { it.categoryAmounts }
            .filter { it.group == AccountGroup.EXPENSE }
            .groupBy { it.category }
            .map { (category, amounts) ->
                category to amounts.sumOf { it.amountMinorUnits }
            }
            .filter { (_, amount) -> amount > 0 }
            .joinToString(" · ") { (category, amount) ->
                "$category ${formatAmount(amount)}"
            }
    }

    private fun RecordSummary.toDetailUiState(): RecordDetailUiState {
        val amountLabel = if (expenseMinorUnits > 0) {
            formatSigned(expenseMinorUnits, '-')
        } else {
            formatSigned(incomeMinorUnits, '+')
        }
        val editType = if (expenseMinorUnits > 0) RecordEditType.EXPENSE else RecordEditType.INCOME
        val amountMinorUnits = if (expenseMinorUnits > 0) expenseMinorUnits else incomeMinorUnits
        return RecordDetailUiState(
            id = id,
            type = type,
            editType = editType,
            category = category,
            categoryAccountId = categoryAmounts.first { it.category == category }.accountId,
            amountLabel = amountLabel,
            amountInput = formatInputAmount(amountMinorUnits),
            note = note,
            time = time,
            occurredAtIso = java.time.Instant.ofEpochMilli(occurredAtEpochMillis).toString()
        )
    }

    private fun AccountGroup.normalDirection(): EntryDirection? = when (this) {
        AccountGroup.INCOME -> EntryDirection.CREDIT
        AccountGroup.EXPENSE -> EntryDirection.DEBIT
        else -> null
    }

    private fun TransactionType.displayName(): String = when (this) {
        TransactionType.INCOME -> "收入"
        TransactionType.EXPENSE -> "支出"
        TransactionType.TRANSFER -> "转账"
        TransactionType.CREDIT_CARD_EXPENSE -> "信用卡支出"
        TransactionType.LIABILITY_REPAYMENT -> "还款"
        TransactionType.LEND -> "借出"
        TransactionType.BORROW -> "借入"
        TransactionType.LOAN_PAYMENT -> "贷款还款"
        TransactionType.INVESTMENT_BUY -> "买入投资"
        TransactionType.INVESTMENT_SELL -> "卖出投资"
        TransactionType.BALANCE_ADJUSTMENT -> "初始余额"
    }

    private fun AccountEntity.displayName(): String = when (id) {
        "salary-income" -> "工资收入"
        "external-income" -> "外部收入"
        "other-income" -> "其他"
        "meal-expense" -> "餐食"
        "shopping-expense" -> "购物"
        "sport-expense" -> "运动"
        "other-expense" -> "其他"
        else -> name
    }

    private fun formatSigned(minorUnits: Long, sign: Char): String = "$sign${formatAmount(minorUnits)}"

    private fun formatCalendarSigned(minorUnits: Long, sign: Char): String {
        val yuan = minorUnits / 100.0
        val amount = if (yuan >= 1000) {
            val compact = yuan / 1000.0
            if (compact % 1.0 == 0.0) {
                String.format(Locale.US, "%.0fk", compact)
            } else {
                String.format(Locale.US, "%.1fk", compact)
            }
        } else if (minorUnits % 100 == 0L) {
            String.format(Locale.US, "%.0f", yuan)
        } else {
            String.format(Locale.US, "%.2f", yuan)
        }
        return "$sign¥$amount"
    }

    private fun formatAmount(minorUnits: Long): String {
        return "¥" + String.format(Locale.US, "%,.2f", minorUnits / 100.0)
    }

    private fun formatInputAmount(minorUnits: Long): String {
        val yuan = minorUnits / 100.0
        return if (minorUnits % 100 == 0L) {
            String.format(Locale.US, "%.0f", yuan)
        } else {
            String.format(Locale.US, "%.2f", yuan)
        }
    }
}

data class RecordsUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val monthLabel: String = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDayTitle: String = selectedDate.format(DateTimeFormatter.ofPattern("MM月dd日")),
    val days: List<RecordCalendarDayUiState> = emptyList(),
    val selectedIncomeLabel: String = "收入 +¥0.00",
    val selectedExpenseLabel: String = "支出 -¥0.00",
    val selectedCategorySummary: String = "",
    val selectedDetails: List<RecordDetailUiState> = emptyList(),
    val editForm: RecordEditFormUiState = RecordEditFormUiState()
)

data class RecordCalendarDayUiState(
    val date: String,
    val dayOfMonth: String,
    val isInVisibleMonth: Boolean,
    val isSelected: Boolean,
    val hasRecords: Boolean,
    val incomeLabel: String,
    val expenseLabel: String,
    val categorySummary: String
)

data class RecordDetailUiState(
    val id: String,
    val type: String,
    val editType: RecordEditType,
    val category: String,
    val categoryAccountId: String,
    val amountLabel: String,
    val amountInput: String,
    val note: String,
    val time: String,
    val occurredAtIso: String
)

data class RecordEditFormUiState(
    val transactionId: String? = null,
    val editType: RecordEditType = RecordEditType.EXPENSE,
    val amountInput: String = "",
    val noteInput: String = "",
    val occurredAtIso: String? = null,
    val selectedIncomeKind: IncomeKind = IncomeKind.SALARY,
    val selectedExpenseKind: ExpenseKind = ExpenseKind.MEAL,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
) {
    val isOpen: Boolean
        get() = transactionId != null
}

enum class RecordEditType {
    EXPENSE,
    INCOME
}

private data class RecordSummary(
    val id: String,
    val type: String,
    val note: String,
    val date: LocalDate,
    val time: String,
    val occurredAtEpochMillis: Long,
    val category: String,
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long,
    val categoryAmounts: List<RecordCategoryAmount>
)

private data class RecordCategoryAmount(
    val accountId: String,
    val category: String,
    val group: AccountGroup,
    val amountMinorUnits: Long
)
