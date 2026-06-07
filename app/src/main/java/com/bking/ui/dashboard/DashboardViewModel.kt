package com.bking.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bking.data.repository.AddTransactionUseCase
import com.bking.data.repository.DashboardRepository
import com.bking.data.repository.DashboardSummary
import com.bking.data.repository.DefaultAccountIds
import com.bking.data.repository.ExpenseKind
import com.bking.data.repository.IncomeKind
import com.bking.domain.service.AmountParser
import com.bking.ui.common.formatCny
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {
    private val formState = MutableStateFlow(AddTransactionFormState())

    val uiState: StateFlow<DashboardUiState> = dashboardRepository.observeSummary()
        .map { it.toUiState() }
        .combine(formState) { dashboard, form ->
            dashboard.copy(form = form)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardSummary().toUiState()
        )

    private fun DashboardSummary.toUiState(): DashboardUiState = DashboardUiState(
        totalAssets = totalAssets.formatCny(),
        totalLiabilities = totalLiabilities.formatCny(),
        netWorth = netWorth.formatCny(),
        monthlyIncome = monthlyIncome.formatCny(),
        monthlyExpense = monthlyExpense.formatCny(),
        monthlySurplus = monthlySurplus.formatCny()
    )

    fun openForm(type: AddTransactionType) {
        formState.value = AddTransactionFormState(
            selectedType = type,
            selectedIncomeKind = IncomeKind.SALARY,
            selectedExpenseKind = ExpenseKind.MEAL,
            amountInput = "",
            noteInput = "",
            isSaving = false
        )
    }

    fun closeForm() {
        formState.value = AddTransactionFormState()
    }

    fun updateAmount(value: String) {
        formState.update { it.copy(amountInput = value, errorMessage = null) }
    }

    fun updateNote(value: String) {
        formState.update { it.copy(noteInput = value, errorMessage = null) }
    }

    fun selectIncomeKind(kind: IncomeKind) {
        formState.update { it.copy(selectedIncomeKind = kind, errorMessage = null) }
    }

    fun selectExpenseKind(kind: ExpenseKind) {
        formState.update { it.copy(selectedExpenseKind = kind, errorMessage = null) }
    }

    fun saveTransaction() {
        val current = formState.value
        val type = current.selectedType ?: return
        val amount = AmountParser.parseCny(current.amountInput)
        if (amount == null) {
            formState.update { it.copy(errorMessage = "请输入大于 0 的金额。") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                when (type) {
                    AddTransactionType.EXPENSE -> addTransactionUseCase.addExpense(
                        amount = amount,
                        note = current.noteInput,
                        kind = current.selectedExpenseKind
                    )
                    AddTransactionType.INCOME -> addTransactionUseCase.addIncome(
                        amount = amount,
                        note = current.noteInput,
                        kind = current.selectedIncomeKind
                    )
                    AddTransactionType.TRANSFER -> addTransactionUseCase.addTransfer(
                        amount = amount,
                        note = current.noteInput,
                        fromAccountId = DefaultAccountIds.CASH,
                        toAccountId = DefaultAccountIds.BANK
                    )
                    AddTransactionType.LIABILITY_REPAYMENT -> addTransactionUseCase.repayLiability(
                        amount = amount,
                        note = current.noteInput,
                        paidFromAccountId = DefaultAccountIds.BANK,
                        liabilityAccountId = DefaultAccountIds.CREDIT_CARD
                    )
                }
            }.onSuccess {
                closeForm()
            }.onFailure { error ->
                formState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "保存失败。"
                    )
                }
            }
        }
    }

}

data class DashboardUiState(
    val totalAssets: String,
    val totalLiabilities: String,
    val netWorth: String,
    val monthlyIncome: String,
    val monthlyExpense: String,
    val monthlySurplus: String,
    val form: AddTransactionFormState = AddTransactionFormState()
)

data class AddTransactionFormState(
    val selectedType: AddTransactionType? = null,
    val selectedIncomeKind: IncomeKind = IncomeKind.SALARY,
    val selectedExpenseKind: ExpenseKind = ExpenseKind.MEAL,
    val amountInput: String = "",
    val noteInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class AddTransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    LIABILITY_REPAYMENT
}
