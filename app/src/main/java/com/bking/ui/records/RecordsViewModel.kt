package com.bking.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bking.data.local.dao.AccountDao
import com.bking.data.local.dao.LedgerEntryDao
import com.bking.data.local.dao.TransactionDao
import com.bking.data.repository.ExpenseKind
import com.bking.data.repository.IncomeKind
import com.bking.data.repository.RecordMutationUseCase
import com.bking.domain.service.AmountParser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecordsViewModel @Inject constructor(
    transactionDao: TransactionDao,
    ledgerEntryDao: LedgerEntryDao,
    accountDao: AccountDao,
    private val recordMutationUseCase: RecordMutationUseCase
) : ViewModel() {
    private val selection = MutableStateFlow(
        RecordsCalendarSelection(
            visibleMonth = YearMonth.now(),
            selectedDate = LocalDate.now()
        )
    )
    private val editForm = MutableStateFlow(RecordEditFormUiState())

    val uiState: StateFlow<RecordsUiState> = combine(
        selection,
        editForm,
        transactionDao.observeAll(),
        ledgerEntryDao.observeAll(),
        accountDao.observeActiveAccounts()
    ) { currentSelection, currentEditForm, transactions, entries, accounts ->
        RecordsCalendarMapper.build(
            visibleMonth = currentSelection.visibleMonth,
            selectedDate = currentSelection.selectedDate,
            transactions = transactions,
            entries = entries,
            accounts = accounts
        ).copy(editForm = currentEditForm)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordsUiState()
        )

    fun selectDate(date: LocalDate) {
        selection.update {
            it.copy(
                visibleMonth = YearMonth.from(date),
                selectedDate = date
            )
        }
    }

    fun showPreviousMonth() {
        selection.update {
            val month = it.visibleMonth.minusMonths(1)
            RecordsCalendarSelection(
                visibleMonth = month,
                selectedDate = month.atDay(1)
            )
        }
    }

    fun showNextMonth() {
        selection.update {
            val month = it.visibleMonth.plusMonths(1)
            RecordsCalendarSelection(
                visibleMonth = month,
                selectedDate = month.atDay(1)
            )
        }
    }

    fun openEdit(detail: RecordDetailUiState) {
        editForm.value = RecordEditFormUiState(
            transactionId = detail.id,
            editType = detail.editType,
            amountInput = detail.amountInput,
            noteInput = if (detail.note == "无备注") "" else detail.note,
            occurredAtIso = detail.occurredAtIso,
            selectedIncomeKind = incomeKindFor(detail.categoryAccountId),
            selectedExpenseKind = expenseKindFor(detail.categoryAccountId)
        )
    }

    fun closeEdit() {
        editForm.value = RecordEditFormUiState()
    }

    fun updateEditAmount(value: String) {
        editForm.update { it.copy(amountInput = value, errorMessage = null) }
    }

    fun updateEditNote(value: String) {
        editForm.update { it.copy(noteInput = value, errorMessage = null) }
    }

    fun selectEditIncomeKind(kind: IncomeKind) {
        editForm.update { it.copy(selectedIncomeKind = kind, errorMessage = null) }
    }

    fun selectEditExpenseKind(kind: ExpenseKind) {
        editForm.update { it.copy(selectedExpenseKind = kind, errorMessage = null) }
    }

    fun saveEdit() {
        val current = editForm.value
        val transactionId = current.transactionId ?: return
        val occurredAt = current.occurredAtIso?.let(Instant::parse) ?: return
        val amount = AmountParser.parseCny(current.amountInput)
        if (amount == null) {
            editForm.update { it.copy(errorMessage = "请输入大于 0 的金额。") }
            return
        }

        viewModelScope.launch {
            editForm.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                when (current.editType) {
                    RecordEditType.EXPENSE -> recordMutationUseCase.updateExpense(
                        transactionId = transactionId,
                        amount = amount,
                        note = current.noteInput,
                        kind = current.selectedExpenseKind,
                        occurredAt = occurredAt
                    )
                    RecordEditType.INCOME -> recordMutationUseCase.updateIncome(
                        transactionId = transactionId,
                        amount = amount,
                        note = current.noteInput,
                        kind = current.selectedIncomeKind,
                        occurredAt = occurredAt
                    )
                }
            }.onSuccess {
                closeEdit()
            }.onFailure { error ->
                editForm.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "保存失败。"
                    )
                }
            }
        }
    }

    fun deleteRecord(transactionId: String) {
        viewModelScope.launch {
            recordMutationUseCase.delete(transactionId)
            if (editForm.value.transactionId == transactionId) {
                closeEdit()
            }
        }
    }

    private fun incomeKindFor(accountId: String): IncomeKind {
        return IncomeKind.entries.firstOrNull { it.accountId == accountId } ?: IncomeKind.OTHER
    }

    private fun expenseKindFor(accountId: String): ExpenseKind {
        return ExpenseKind.entries.firstOrNull { it.accountId == accountId } ?: ExpenseKind.OTHER
    }
}

private data class RecordsCalendarSelection(
    val visibleMonth: YearMonth,
    val selectedDate: LocalDate
)
