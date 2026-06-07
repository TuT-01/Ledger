package com.bking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import java.time.Instant

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val group: AccountGroup,
    val type: AccountType,
    val currencyCode: String,
    val openingBalanceMinorUnits: Long,
    val isArchived: Boolean,
    val createdAt: Instant
)

