package com.bking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bking.domain.model.TransactionType
import java.time.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val occurredAt: Instant,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

