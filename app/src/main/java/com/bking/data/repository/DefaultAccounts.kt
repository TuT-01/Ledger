package com.bking.data.repository

import com.bking.data.local.entity.AccountEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import java.time.Instant

object DefaultAccounts {
    fun create(createdAt: Instant): List<AccountEntity> = items.map { item ->
        AccountEntity(
            id = item.id,
            name = item.name,
            group = item.group,
            type = item.type,
            currencyCode = "CNY",
            openingBalanceMinorUnits = 0,
            isArchived = false,
            createdAt = createdAt
        )
    }

    val items: List<DefaultAccountItem> = listOf(
        DefaultAccountItem("cash", "现金钱包", AccountGroup.ASSET, AccountType.CASH),
        DefaultAccountItem("salary-income", "工资收入", AccountGroup.INCOME, AccountType.INCOME_CATEGORY),
        DefaultAccountItem("external-income", "外部收入", AccountGroup.INCOME, AccountType.INCOME_CATEGORY),
        DefaultAccountItem("other-income", "其他", AccountGroup.INCOME, AccountType.INCOME_CATEGORY),
        DefaultAccountItem("meal-expense", "餐食", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY),
        DefaultAccountItem("shopping-expense", "购物", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY),
        DefaultAccountItem("sport-expense", "运动", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY),
        DefaultAccountItem("other-expense", "其他", AccountGroup.EXPENSE, AccountType.EXPENSE_CATEGORY)
    )
}

data class DefaultAccountItem(
    val id: String,
    val name: String,
    val group: AccountGroup,
    val type: AccountType
)
