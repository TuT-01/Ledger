package com.bking.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bking.data.local.BkingDatabase
import com.bking.data.local.entity.AccountEntity
import com.bking.domain.model.AccountGroup
import com.bking.domain.model.AccountType
import com.bking.domain.model.Money
import com.bking.domain.model.TransactionType
import com.bking.domain.service.DoubleEntryFactory
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class LedgerRepositoryTest {
    private lateinit var database: BkingDatabase
    private lateinit var repository: LedgerRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BkingDatabase::class.java
        ).build()
        repository = LedgerRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createsTransactionAndEntriesAtomically() = runBlocking {
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
        val draft = DoubleEntryFactory.expense(
            amount = Money.cnyCents(1800),
            paidFromAccountId = "bank",
            expenseAccountId = "food",
            occurredAt = now,
            note = "午餐"
        )

        repository.createTransaction(id = "txn-1", draft = draft, now = now)

        val transaction = database.transactionDao().getById("txn-1")
        val entries = database.ledgerEntryDao().getByTransactionId("txn-1")

        assertNotNull(transaction)
        assertEquals(TransactionType.EXPENSE, transaction?.type)
        assertEquals(2, entries.size)
        assertEquals("txn-1:1", entries[0].id)
        assertEquals("txn-1:2", entries[1].id)
    }
}

