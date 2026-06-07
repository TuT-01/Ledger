package com.bking.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bking.R
import com.bking.ui.dashboard.DashboardScreen
import com.bking.ui.dashboard.DashboardViewModel
import com.bking.ui.loans.LoanViewModel
import com.bking.ui.loans.LoansScreen
import com.bking.ui.profile.MoreScreen
import com.bking.ui.profile.MoreViewModel
import com.bking.ui.profile.ProfileUiState
import com.bking.ui.profile.ProfileViewModel
import com.bking.ui.profile.StartupDestination
import com.bking.ui.profile.startupDestination
import com.bking.ui.records.RecordsScreen
import com.bking.ui.records.RecordsViewModel
import com.bking.ui.stats.StatsScreen
import com.bking.ui.stats.StatsViewModel

@Composable
fun BkingApp(
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        when (profileState.startupDestination) {
            StartupDestination.SPLASH -> StartupScreen()
            StartupDestination.REGISTRATION -> RegistrationScreen(
                uiState = profileState,
                onEmailChange = profileViewModel::updateEmail,
                onDisplayNameChange = profileViewModel::updateDisplayName,
                onRegister = profileViewModel::register
            )
            StartupDestination.MAIN_APP -> MainAppContent(profileState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(profileState: ProfileUiState) {
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(selectedTab.title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            AppTab.DASHBOARD -> DashboardRoute(contentModifier)
            AppTab.RECORDS -> RecordsRoute(contentModifier)
            AppTab.STATS -> StatsRoute(contentModifier)
            AppTab.LOANS -> LoansRoute(contentModifier)
            AppTab.MORE -> MoreRoute(profileState, contentModifier)
        }
    }
}

@Composable
private fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        uiState = state,
        onOpenForm = viewModel::openForm,
        onAmountChange = viewModel::updateAmount,
        onNoteChange = viewModel::updateNote,
        onIncomeKindChange = viewModel::selectIncomeKind,
        onExpenseKindChange = viewModel::selectExpenseKind,
        onSave = viewModel::saveTransaction,
        onCancel = viewModel::closeForm,
        modifier = modifier
    )
}

@Composable
private fun RecordsRoute(
    modifier: Modifier = Modifier,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecordsScreen(
        uiState = state,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onSelectDate = viewModel::selectDate,
        onOpenEdit = viewModel::openEdit,
        onCloseEdit = viewModel::closeEdit,
        onEditAmountChange = viewModel::updateEditAmount,
        onEditNoteChange = viewModel::updateEditNote,
        onEditIncomeKindChange = viewModel::selectEditIncomeKind,
        onEditExpenseKindChange = viewModel::selectEditExpenseKind,
        onSaveEdit = viewModel::saveEdit,
        onDeleteRecord = viewModel::deleteRecord,
        modifier = modifier
    )
}

@Composable
private fun StatsRoute(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        uiState = state,
        onModeChange = viewModel::setMode,
        onStartChange = viewModel::updateStart,
        modifier = modifier
    )
}

@Composable
private fun LoansRoute(
    modifier: Modifier = Modifier,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoansScreen(
        uiState = state,
        onNameChange = viewModel::updateName,
        onPrincipalChange = viewModel::updatePrincipal,
        onRateChange = viewModel::updateRate,
        onTermChange = viewModel::updateTerm,
        onStartDateChange = viewModel::updateStartDate,
        onRepaymentDayChange = viewModel::updateRepaymentDay,
        onMethodChange = viewModel::updateMethod,
        onSaveLoan = viewModel::saveLoan,
        onRecordNextPayment = viewModel::recordNextPayment,
        onCloseLoan = viewModel::closeLoan,
        modifier = modifier
    )
}

@Composable
private fun MoreRoute(
    profileState: ProfileUiState,
    modifier: Modifier = Modifier,
    viewModel: MoreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MoreScreen(
        profileState = profileState,
        uiState = state,
        onAccountNameChange = viewModel::updateAccountName,
        onAccountGroupChange = viewModel::updateAccountGroup,
        onSaveAccount = viewModel::saveAccount,
        onArchiveAccount = viewModel::archiveAccount,
        onBudgetCategoryChange = viewModel::updateBudgetCategory,
        onBudgetAmountChange = viewModel::updateBudgetAmount,
        onSaveBudget = viewModel::saveBudget,
        onExportBackup = viewModel::exportBackup,
        onImportTextChange = viewModel::updateImportText,
        onImportBackup = viewModel::importBackup,
        modifier = modifier
    )
}

@Composable
private fun StartupScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
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
            Text("正在进入账本")
        }
    }
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
            Text("注册 Bking", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("用邮箱创建本机档案，之后就可以开始管理收入、支出、账户、预算和贷款。")
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
                Text(uiState.registration.errorMessage, color = MaterialTheme.colorScheme.error)
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

private enum class AppTab(
    val title: String,
    val label: String,
    val icon: String
) {
    DASHBOARD("首页", "首页", "H"),
    RECORDS("记录", "记录", "R"),
    STATS("统计", "统计", "S"),
    LOANS("贷款", "贷款", "L"),
    MORE("我的", "我的", "M")
}
