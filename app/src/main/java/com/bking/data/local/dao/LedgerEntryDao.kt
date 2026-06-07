package com.bking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.model.LedgerEntryWithTransaction
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entries: List<LedgerEntryEntity>)

    @Query("SELECT * FROM ledger_entries WHERE transactionId = :transactionId ORDER BY id ASC")
    suspend fun getByTransactionId(transactionId: String): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries WHERE accountId = :accountId ORDER BY id ASC")
    suspend fun getByAccountId(accountId: String): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries ORDER BY id ASC")
    suspend fun getAll(): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries ORDER BY id ASC")
    fun observeAll(): Flow<List<LedgerEntryEntity>>

    @Query("DELETE FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: String)

    @Query(
        """
        SELECT
            ledger_entries.id AS id,
            ledger_entries.transactionId AS transactionId,
            ledger_entries.accountId AS accountId,
            ledger_entries.direction AS direction,
            ledger_entries.amountMinorUnits AS amountMinorUnits,
            ledger_entries.currencyCode AS currencyCode,
            transactions.occurredAt AS transactionOccurredAt
        FROM ledger_entries
        INNER JOIN transactions ON transactions.id = ledger_entries.transactionId
        WHERE transactions.occurredAt >= :start
            AND transactions.occurredAt < :endExclusive
        ORDER BY transactions.occurredAt DESC, ledger_entries.id ASC
        """
    )
    fun observeEntriesBetween(
        start: Instant,
        endExclusive: Instant
    ): Flow<List<LedgerEntryWithTransaction>>
}
