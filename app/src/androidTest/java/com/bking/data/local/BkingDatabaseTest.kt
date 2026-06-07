package com.bking.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.TransactionEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.TransactionType
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BkingDatabaseTest {
    private lateinit var database: BkingDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BkingDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun storesTransactionWithBalancedEntries() = runBlocking {
        val now = Instant.parse("2026-05-12T10:00:00Z")
        database.accountDao().insertAll(
            listOf(
                AccountEntity(
                    id = "bank",
                    name = "银行卡",
                    group = AccountGroup.ASSET,
                    type = AccountType.BANK,
                    currencyCode = "CNY",
                    openingBalanceMinorUnits = 0,
                    isArchived = false,
                    createdAt = now
                ),
                AccountEntity(
                    id = "food",
                    name = "餐饮",
                    group = AccountGroup.EXPENSE,
                    type = AccountType.EXPENSE_CATEGORY,
                    currencyCode = "CNY",
                    openingBalanceMinorUnits = 0,
                    isArchived = false,
                    createdAt = now
                )
            )
        )
        database.transactionDao().insert(
            TransactionEntity(
                id = "txn-1",
                type = TransactionType.EXPENSE,
                occurredAt = now,
                note = "午餐",
                createdAt = now,
                updatedAt = now
            )
        )
        database.ledgerEntryDao().insertAll(
            listOf(
                LedgerEntryEntity(
                    id = "entry-1",
                    transactionId = "txn-1",
                    accountId = "food",
                    direction = EntryDirection.DEBIT,
                    amountMinorUnits = 1800,
                    currencyCode = "CNY"
                ),
                LedgerEntryEntity(
                    id = "entry-2",
                    transactionId = "txn-1",
                    accountId = "bank",
                    direction = EntryDirection.CREDIT,
                    amountMinorUnits = 1800,
                    currencyCode = "CNY"
                )
            )
        )

        val entries = database.ledgerEntryDao().getByTransactionId("txn-1")

        assertEquals(2, entries.size)
        assertEquals("food", entries[0].accountId)
        assertEquals("bank", entries[1].accountId)
    }
}

