package com.bking.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bking.data.repository.CategoryBreakdown
import com.bking.data.repository.StatsRepository
import com.bking.data.repository.StatsSummary
import com.bking.domain.model.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val query = MutableStateFlow(
        StatsQuery(
            mode = StatsMode.MONTH,
            startInput = LocalDate.now().toString()
        )
    )

    val uiState: StateFlow<StatsUiState> = query
        .flatMapLatest { current ->
            val range = current.range()
            if (range == null) {
                MutableStateFlow(StatsSummary().toUiState(current, null, "Invalid start."))
            } else {
                statsRepository.observeSummary(range.start, range.endExclusive)
                    .map { it.toUiState(current, range, null) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsSummary().toUiState(query.value, query.value.range(), null)
        )

    fun setMode(mode: StatsMode) {
        query.value = when (mode) {
            StatsMode.WEEK -> StatsQuery(StatsMode.WEEK, LocalDate.now().toString())
            StatsMode.MONTH -> StatsQuery(StatsMode.MONTH, LocalDate.now().toString())
            StatsMode.YEAR -> StatsQuery(StatsMode.YEAR, LocalDate.now().toString())
        }
    }

    fun updateStart(input: String) {
        query.update { it.copy(startInput = input) }
    }

    private fun StatsSummary.toUiState(
        query: StatsQuery,
        range: StatsRange?,
        error: String?
    ): StatsUiState = StatsUiState(
        mode = query.mode,
        startInput = query.startInput,
        endLabel = range?.endLabel.orEmpty(),
        income = income.format(),
        expense = expense.format(),
        surplus = surplus.format(),
        incomeSlices = incomeBreakdown.toSlices(income),
        expenseSlices = expenseBreakdown.toSlices(expense),
        errorMessage = error
    )

    private fun List<CategoryBreakdown>.toSlices(total: Money): List<PieSliceUiState> {
        if (total.minorUnits <= 0) {
            return emptyList()
        }
        return mapIndexed { index, item ->
            PieSliceUiState(
                label = item.label,
                amount = item.amount.format(),
                percent = item.amount.minorUnits.toFloat() / total.minorUnits.toFloat(),
                colorIndex = index
            )
        }
    }

    private fun StatsQuery.range(): StatsRange? = when (mode) {
        StatsMode.WEEK -> StatsPeriod.week(startInput)
        StatsMode.MONTH -> StatsPeriod.month(startInput)
        StatsMode.YEAR -> StatsPeriod.year(startInput)
    }

    private fun Money.format(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(minorUnits / 100.0)
    }
}

data class StatsQuery(
    val mode: StatsMode,
    val startInput: String
)

data class StatsUiState(
    val mode: StatsMode,
    val startInput: String,
    val endLabel: String,
    val income: String,
    val expense: String,
    val surplus: String,
    val incomeSlices: List<PieSliceUiState>,
    val expenseSlices: List<PieSliceUiState>,
    val errorMessage: String?
)

data class PieSliceUiState(
    val label: String,
    val amount: String,
    val percent: Float,
    val colorIndex: Int
)

enum class StatsMode {
    WEEK,
    MONTH,
    YEAR
}
