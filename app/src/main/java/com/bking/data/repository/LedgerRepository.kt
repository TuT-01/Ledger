package com.bking.data.repository

import androidx.room.withTransaction
import com.bking.data.local.BkingDatabase
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.TransactionEntity
import com.bking.domain.model.TransactionDraft
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerRepository @Inject constructor(
    private val database: BkingDatabase
) {
    suspend fun createTransaction(
        id: String,
        draft: TransactionDraft,
        now: Instant = Instant.now()
    ) {
        require(id.isNotBlank()) { "Transaction id cannot be blank." }

        database.withTransaction {
            database.transactionDao().insert(
                TransactionEntity(
                    id = id,
                    type = draft.type,
                    occurredAt = draft.occurredAt,
                    note = draft.note,
                    createdAt = now,
                    updatedAt = now
                )
            )
            database.ledgerEntryDao().insertAll(
                draft.entries.mapIndexed { index, entry ->
                    LedgerEntryEntity(
                        id = "$id:${index + 1}",
                        transactionId = id,
                        accountId = entry.accountId,
                        direction = entry.direction,
                        amountMinorUnits = entry.amount.minorUnits,
                        currencyCode = entry.amount.currencyCode
                    )
                }
            )
        }
    }

    suspend fun replaceTransaction(
        id: String,
        draft: TransactionDraft,
        now: Instant = Instant.now()
    ) {
        require(id.isNotBlank()) { "Transaction id cannot be blank." }

        database.withTransaction {
            database.transactionDao().updateCore(
                id = id,
                type = draft.type,
                occurredAt = draft.occurredAt,
                note = draft.note,
                updatedAt = now
            )
            database.ledgerEntryDao().deleteByTransactionId(id)
            database.ledgerEntryDao().insertAll(
                draft.entries.mapIndexed { index, entry ->
                    LedgerEntryEntity(
                        id = "$id:${index + 1}",
                        transactionId = id,
                        accountId = entry.accountId,
                        direction = entry.direction,
                        amountMinorUnits = entry.amount.minorUnits,
                        currencyCode = entry.amount.currencyCode
                    )
                }
            )
        }
    }

    suspend fun deleteTransaction(id: String) {
        require(id.isNotBlank()) { "Transaction id cannot be blank." }
        database.transactionDao().deleteById(id)
    }
}
