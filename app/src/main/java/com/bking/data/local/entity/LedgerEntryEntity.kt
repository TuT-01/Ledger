package com.bking.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bking.domain.model.EntryDirection

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["accountId"])
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val accountId: String,
    val direction: EntryDirection,
    val amountMinorUnits: Long,
    val currencyCode: String
)

