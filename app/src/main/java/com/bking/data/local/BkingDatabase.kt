package com.bking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bking.data.local.dao.AccountDao
import com.bking.data.local.dao.LedgerEntryDao
import com.bking.data.local.dao.TransactionDao
import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        LedgerEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(BkingTypeConverters::class)
abstract class BkingDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
}

