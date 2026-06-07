package com.bking.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bking.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bking.data.repository.ExpenseKind
import com.bking.data.repository.IncomeKind
import com.bking.ui.dashboard.AddTransactionFormState
import com.bking.ui.dashboard.AddTransactionType
import com.bking.ui.dashboard.DashboardUiState
import com.bking.ui.dashboard.DashboardViewModel
import com.bking.ui.profile.ProfileUiState
import com.bking.ui.profile.ProfileViewModel
import com.bking.ui.profile.StartupDestination
import com.bking.ui.profile.startupDestination
import com.bking.ui.records.RecordCalendarDayUiState
import com.bking.ui.records.RecordDetailUiState
import com.bking.ui.records.RecordEditFormUiState
import com.bking.ui.records.RecordEditType
import com.bking.ui.records.RecordsUiState
import com.bking.ui.records.RecordsViewModel
import com.bking.ui.stats.StatsMode
import com.bking.ui.stats.PieSliceUiState
import com.bking.ui.stats.StatsUiState
import com.bking.ui.stats.StatsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BkingApp(
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    Log.d("BkingStartup", "BkingApp destination=${profileState.startupDestination} email=${profileState.email.isNotBlank()}")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        when (profileState.startupDestination) {
            StartupDestination.SPLASH -> {
                StartupScreen()
            }
            StartupDestination.REGISTRATION -> {
                RegistrationScreen(
                    uiState = profileState,
                    onEmailChange = profileViewModel::updateEmail,
                    onDisplayNameChange = profileViewModel::updateDisplayName,
                    onRegister = profileViewModel::register
                )
            }
            StartupDestination.MAIN_APP -> MainAppContent(profileState = profileState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    profileState: ProfileUiState
) {
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    Log.d("BkingStartup", "MainAppContent selectedTab=$selectedTab")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(selectedTab.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.icon) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (selectedTab) {
            AppTab.DASHBOARD -> DashboardRoute(modifier = contentModifier)
            AppTab.TRANSACTIONS -> TransactionsRoute(modifier = contentModifier)
            AppTab.STATS -> StatsRoute(modifier = contentModifier)
            AppTab.PROFILE -> ProfileScreen(uiState = profileState, modifier = contentModifier)
        }
    }
}

@Composable
private fun DashboardRoute(
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    Log.d("BkingStartup", "DashboardRoute netWorth=${dashboardState.netWorth}")

    DashboardScreen(
        uiState = dashboardState,
        onOpenForm = dashboardViewModel::openForm,
        onAmountChange = dashboardViewModel::updateAmount,
        onNoteChange = dashboardViewModel::updateNote,
        onIncomeKindChange = dashboardViewModel::selectIncomeKind,
        onExpenseKindChange = dashboardViewModel::selectExpenseKind,
        onSave = dashboardViewModel::saveTransaction,
        onCancel = dashboardViewModel::closeForm,
        modifier = modifier
    )
}

@Composable
private fun TransactionsRoute(
    modifier: Modifier = Modifier,
    recordsViewModel: RecordsViewModel = hiltViewModel()
) {
    val recordsState by recordsViewModel.uiState.collectAsStateWithLifecycle()

    TransactionsScreen(
        uiState = recordsState,
        onPreviousMonth = recordsViewModel::showPreviousMonth,
        onNextMonth = recordsViewModel::showNextMonth,
        onSelectDate = recordsViewModel::selectDate,
        onOpenEdit = recordsViewModel::openEdit,
        onCloseEdit = recordsViewModel::closeEdit,
        onEditAmountChange = recordsViewModel::updateEditAmount,
        onEditNoteChange = recordsViewModel::updateEditNote,
        onEditIncomeKindChange = recordsViewModel::selectEditIncomeKind,
        onEditExpenseKindChange = recordsViewModel::selectEditExpenseKind,
        onSaveEdit = recordsViewModel::saveEdit,
        onDeleteRecord = recordsViewModel::deleteRecord,
        modifier = modifier
    )
}

@Composable
private fun StatsRoute(
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel = hiltViewModel()
) {
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()

    StatsScreen(
        uiState = statsState,
        onModeChange = statsViewModel::setMode,
        onStartChange = statsViewModel::updateStart,
        modifier = modifier
    )
}

@Composable
private fun StartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Bking",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在进入记账",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private enum class AppTab(
    val title: String,
    val label: String,
    val icon: String
) {
    DASHBOARD("记账", "首页", "◎"),
    TRANSACTIONS("记录", "记录", "≡"),
    STATS("统计", "统计", "∑"),
    PROFILE("我的", "我的", "@")
}

@Composable
private fun RegistrationScreen(
    uiState: ProfileUiState,
    onEmailChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onRegister: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "注册 Bking",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "用邮箱创建本机档案，之后就可以开始手动记录收入和支出。",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = uiState.registration.emailInput,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("邮箱") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = uiState.registration.displayNameInput,
                onValueChange = onDisplayNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("昵称（可选）") }
            )
            if (uiState.registration.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.registration.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(
                onClick = onRegister,
                enabled = !uiState.registration.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.registration.isSaving) "注册中" else "使用邮箱注册")
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenForm: (AddTransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onIncomeKindChange: (IncomeKind) -> Unit,
    onExpenseKindChange: (ExpenseKind) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Bking 已进入主页",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            NetWorthHeader(
                totalAssets = uiState.totalAssets,
                totalLiabilities = uiState.totalLiabilities,
                netWorth = uiState.netWorth
            )
        }

        item {
            MonthlySummary(
                income = uiState.monthlyIncome,
                expense = uiState.monthlyExpense,
                surplus = uiState.monthlySurplus
            )
        }

        item {
            QuickActions(onOpenForm = onOpenForm)
        }

        if (uiState.form.selectedType != null) {
            item {
                AddTransactionForm(
                    form = uiState.form,
                    onAmountChange = onAmountChange,
                    onNoteChange = onNoteChange,
                    onIncomeKindChange = onIncomeKindChange,
                    onExpenseKindChange = onExpenseKindChange,
                    onSave = onSave,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun NetWorthHeader(
    totalAssets: String,
    totalLiabilities: String,
    netWorth: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "当前余额",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = netWorth,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricText(label = "现金资产", value = totalAssets, onPrimary = true)
                MetricText(label = "待调整", value = totalLiabilities, onPrimary = true)
            }
        }
    }
}

@Composable
private fun MonthlySummary(
    income: String,
    expense: String,
    surplus: String,
    title: String = "本月"
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("收入", income, Modifier.weight(1f))
            SummaryCard("支出", expense, Modifier.weight(1f))
            SummaryCard("结余", surplus, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActions(
    onOpenForm: (AddTransactionType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "快速记录",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = { onOpenForm(AddTransactionType.EXPENSE) }) {
                Text("支出")
            }
            FilledTonalButton(onClick = { onOpenForm(AddTransactionType.INCOME) }) {
                Text("收入")
            }
        }
    }
}

@Composable
private fun AddTransactionForm(
    form: AddTransactionFormState,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onIncomeKindChange: (IncomeKind) -> Unit,
    onExpenseKindChange: (ExpenseKind) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = form.selectedType?.title().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            CategorySelector(
                form = form,
                onIncomeKindChange = onIncomeKindChange,
                onExpenseKindChange = onExpenseKindChange
            )
            OutlinedTextField(
                value = form.amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("金额") },
                prefix = { Text("¥") }
            )
            OutlinedTextField(
                value = form.noteInput,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("备注") }
            )
            if (form.errorMessage != null) {
                Text(
                    text = form.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel, enabled = !form.isSaving) {
                    Text("取消")
                }
                FilledTonalButton(onClick = onSave, enabled = !form.isSaving) {
                    Text(if (form.isSaving) "保存中" else "保存")
                }
            }
        }
    }
}

private fun AddTransactionType.title(): String = when (this) {
    AddTransactionType.EXPENSE -> "新增支出"
    AddTransactionType.INCOME -> "新增收入"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    form: AddTransactionFormState,
    onIncomeKindChange: (IncomeKind) -> Unit,
    onExpenseKindChange: (ExpenseKind) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (form.selectedType) {
            AddTransactionType.INCOME -> IncomeKind.entries.forEach { kind ->
                AssistChip(
                    onClick = { onIncomeKindChange(kind) },
                    label = {
                        Text(
                            if (kind == form.selectedIncomeKind) {
                                "${kind.icon} ${kind.label} ✓"
                            } else {
                                "${kind.icon} ${kind.label}"
                            }
                        )
                    }
                )
            }
            AddTransactionType.EXPENSE -> ExpenseKind.entries.forEach { kind ->
                AssistChip(
                    onClick = { onExpenseKindChange(kind) },
                    label = {
                        Text(
                            if (kind == form.selectedExpenseKind) {
                                "${kind.icon} ${kind.label} ✓"
                            } else {
                                "${kind.icon} ${kind.label}"
                            }
                        )
                    }
                )
            }
            null -> Unit
        }
    }
}

@Composable
private fun TransactionsScreen(
    uiState: RecordsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onOpenEdit: (RecordDetailUiState) -> Unit,
    onCloseEdit: () -> Unit,
    onEditAmountChange: (String) -> Unit,
    onEditNoteChange: (String) -> Unit,
    onEditIncomeKindChange: (IncomeKind) -> Unit,
    onEditExpenseKindChange: (ExpenseKind) -> Unit,
    onSaveEdit: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CalendarMonthHeader(
                monthLabel = uiState.monthLabel,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }
        item {
            CalendarGrid(
                days = uiState.days,
                onSelectDate = onSelectDate
            )
        }
        item {
            SelectedDaySummary(uiState = uiState)
        }
        if (uiState.editForm.isOpen) {
            item {
                RecordEditForm(
                    form = uiState.editForm,
                    onAmountChange = onEditAmountChange,
                    onNoteChange = onEditNoteChange,
                    onIncomeKindChange = onEditIncomeKindChange,
                    onExpenseKindChange = onEditExpenseKindChange,
                    onSave = onSaveEdit,
                    onCancel = onCloseEdit
                )
            }
        }
        if (uiState.selectedDetails.isEmpty()) {
            item {
                InfoCard(
                    title = uiState.selectedDayTitle,
                    body = "这一天还没有收入或支出记录。"
                )
            }
        } else {
            uiState.selectedDetails.forEach { detail ->
                item {
                    RecordDetailCard(
                        detail = detail,
                        onOpenEdit = onOpenEdit,
                        onDeleteRecord = onDeleteRecord
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthHeader(
    monthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPreviousMonth) {
            Text("‹")
        }
        Text(
            text = monthLabel,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onNextMonth) {
            Text("›")
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<RecordCalendarDayUiState>,
    onSelectDate: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        onSelectDate = onSelectDate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: RecordCalendarDayUiState,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        day.isSelected -> MaterialTheme.colorScheme.primaryContainer
        day.hasRecords -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (day.isInVisibleMonth) 1f else 0.38f

    Surface(
        modifier = modifier
            .height(88.dp)
            .clickable { onSelectDate(LocalDate.parse(day.date)) },
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = day.dayOfMonth,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (day.isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (day.expenseLabel.isNotBlank()) {
                Text(
                    text = day.expenseLabel,
                    color = MaterialTheme.colorScheme.error.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            if (day.incomeLabel.isNotBlank()) {
                Text(
                    text = day.incomeLabel,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SelectedDaySummary(uiState: RecordsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = uiState.selectedDayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyMetric(
                    label = "收入",
                    value = uiState.selectedIncomeLabel.removePrefix("收入 "),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                DailyMetric(
                    label = "支出",
                    value = uiState.selectedExpenseLabel.removePrefix("支出 "),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.selectedCategorySummary.isNotBlank()) {
                Text(
                    text = uiState.selectedCategorySummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DailyMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun RecordEditForm(
    form: RecordEditFormUiState,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onIncomeKindChange: (IncomeKind) -> Unit,
    onExpenseKindChange: (ExpenseKind) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "修改记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            RecordEditCategorySelector(
                form = form,
                onIncomeKindChange = onIncomeKindChange,
                onExpenseKindChange = onExpenseKindChange
            )
            OutlinedTextField(
                value = form.amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("金额") },
                prefix = { Text("¥") }
            )
            OutlinedTextField(
                value = form.noteInput,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("备注") }
            )
            if (form.errorMessage != null) {
                Text(
                    text = form.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel, enabled = !form.isSaving) {
                    Text("取消")
                }
                FilledTonalButton(onClick = onSave, enabled = !form.isSaving) {
                    Text(if (form.isSaving) "保存中" else "保存修改")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordEditCategorySelector(
    form: RecordEditFormUiState,
    onIncomeKindChange: (IncomeKind) -> Unit,
    onExpenseKindChange: (ExpenseKind) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (form.editType) {
            RecordEditType.INCOME -> IncomeKind.entries.forEach { kind ->
                AssistChip(
                    onClick = { onIncomeKindChange(kind) },
                    label = {
                        Text(if (kind == form.selectedIncomeKind) "${kind.icon} ${kind.label} ✓" else "${kind.icon} ${kind.label}")
                    }
                )
            }
            RecordEditType.EXPENSE -> ExpenseKind.entries.forEach { kind ->
                AssistChip(
                    onClick = { onExpenseKindChange(kind) },
                    label = {
                        Text(if (kind == form.selectedExpenseKind) "${kind.icon} ${kind.label} ✓" else "${kind.icon} ${kind.label}")
                    }
                )
            }
        }
    }
}

@Composable
private fun RecordDetailCard(
    detail: RecordDetailUiState,
    onOpenEdit: (RecordDetailUiState) -> Unit,
    onDeleteRecord: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${detail.category} · ${detail.type}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${detail.time} · ${detail.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = detail.amountLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onOpenEdit(detail) }) {
                    Text("编辑")
                }
                TextButton(onClick = { onDeleteRecord(detail.id) }) {
                    Text("删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsScreen(
    uiState: StatsUiState,
    onModeChange: (StatsMode) -> Unit,
    onStartChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "周期",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = { onModeChange(StatsMode.WEEK) }) {
                    Text(if (uiState.mode == StatsMode.WEEK) "周 ✓" else "周")
                }
                FilledTonalButton(onClick = { onModeChange(StatsMode.MONTH) }) {
                    Text(if (uiState.mode == StatsMode.MONTH) "月 ✓" else "月")
                }
                FilledTonalButton(onClick = { onModeChange(StatsMode.YEAR) }) {
                    Text(if (uiState.mode == StatsMode.YEAR) "年 ✓" else "年")
                }
            }
        }
        item {
            OutlinedTextField(
                value = uiState.startInput,
                onValueChange = onStartChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(uiState.mode.startInputLabel())
                }
            )
        }
        item {
            Text(
                text = "结束默认到${uiState.mode.nextUnitLabel()}：${uiState.endLabel}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (uiState.errorMessage != null) {
            item {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item {
            MonthlySummary(
                income = uiState.income,
                expense = uiState.expense,
                surplus = uiState.surplus,
                title = "周期汇总"
            )
        }
        item {
            PieChartCard(
                title = "支出结构",
                emptyText = "这个周期还没有支出记录。",
                slices = uiState.expenseSlices
            )
        }
        item {
            PieChartCard(
                title = "收入结构",
                emptyText = "这个周期还没有收入记录。",
                slices = uiState.incomeSlices
            )
        }
        item {
            InfoCard(
                title = "统计口径",
                body = "按记录发生时间统计所选周期内的收入、支出和结余。"
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                title = "个人信息",
                body = "邮箱：${uiState.email}\n昵称：${uiState.displayName.ifBlank { "未填写" }}"
            )
        }
        item {
            InfoCard(
                title = "数据",
                body = "当前数据保存在本机，记账方式以手动记录收入和支出为主。"
            )
        }
        item {
            InfoCard(
                title = "分类",
                body = "收入：工资收入、外部收入。\n支出：餐食、购物、运动。"
            )
        }
    }
}

@Composable
private fun PieChartCard(
    title: String,
    emptyText: String,
    slices: List<PieSliceUiState>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (slices.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        slices = slices,
                        modifier = Modifier.size(136.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slices.forEach { slice ->
                            PieLegendRow(slice)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    slices: List<PieSliceUiState>,
    modifier: Modifier = Modifier
) {
    val colors = pieChartColors()
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val topLeft = androidx.compose.ui.geometry.Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f
        )
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = 360f * slice.percent
            drawArc(
                color = colors[slice.colorIndex % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = Size(diameter, diameter)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun PieLegendRow(slice: PieSliceUiState) {
    val colors = pieChartColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape),
            color = colors[slice.colorIndex % colors.size],
            content = {}
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = slice.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${slice.amount} · ${(slice.percent * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun pieChartColors(): List<Color> = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.error,
    MaterialTheme.colorScheme.primaryContainer
)

private fun StatsMode.startInputLabel(): String = when (this) {
    StatsMode.WEEK -> "起始日期 YYYY-MM-DD"
    StatsMode.MONTH -> "起始日期 YYYY-MM-DD"
    StatsMode.YEAR -> "起始日期 YYYY-MM-DD"
}

private fun StatsMode.nextUnitLabel(): String = when (this) {
    StatsMode.WEEK -> "下一周"
    StatsMode.MONTH -> "下一月"
    StatsMode.YEAR -> "下一年"
}

@Composable
private fun InfoCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MetricText(
    label: String,
    value: String,
    onPrimary: Boolean
) {
    val textColor = if (onPrimary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = textColor.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
