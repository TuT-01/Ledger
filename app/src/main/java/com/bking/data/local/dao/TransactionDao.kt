package com.bking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bking.data.local.entity.TransactionEntity
import com.bking.domain.model.TransactionType
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        UPDATE transactions
        SET type = :type,
            occurredAt = :occurredAt,
            note = :note,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateCore(
        id: String,
        type: TransactionType,
        occurredAt: Instant,
        note: String,
        updatedAt: Instant
    )

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}
