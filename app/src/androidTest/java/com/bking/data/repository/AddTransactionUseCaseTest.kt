package com.bking.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bking.data.local.BkingDatabase
import com.bking.domain.model.EntryDirection
import com.bking.domain.model.Money
import com.bking.domain.model.TransactionType
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddTransactionUseCaseTest {
    private lateinit var database: BkingDatabase
    private lateinit var useCase: AddTransactionUseCase

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BkingDatabase::class.java
        ).build()
        AccountSeedRepository(database).seedDefaultsIfNeeded(
            Instant.parse("2026-05-12T10:00:00Z")
        )
        useCase = AddTransactionUseCase(
            ledgerRepository = LedgerRepository(database),
            accountSeedRepository = AccountSeedRepository(database)
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addsMealExpenseUsingCashAccount() = runBlocking {
        useCase.addExpense(
            amount = Money.cnyCents(1234),
            note = "Lunch",
            kind = ExpenseKind.MEAL,
            occurredAt = Instant.parse("2026-05-12T12:00:00Z")
        )

        val transaction = database.transactionDao().getRecent(1).single()
        val entries = database.ledgerEntryDao().getByTransactionId(transaction.id)

        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals("Lunch", transaction.note)
        assertEquals("meal-expense", entries[0].accountId)
        assertEquals(EntryDirection.DEBIT, entries[0].direction)
        assertEquals("cash", entries[1].accountId)
        assertEquals(EntryDirection.CREDIT, entries[1].direction)
    }

    @Test
    fun addsExternalIncomeUsingCashAccount() = runBlocking {
        useCase.addIncome(
            amount = Money.cnyCents(800000),
            note = "Freelance",
            kind = IncomeKind.EXTERNAL,
            occurredAt = Instant.parse("2026-05-12T09:00:00Z")
        )

        val transaction = database.transactionDao().getRecent(1).single()
        val entries = database.ledgerEntryDao().getByTransactionId(transaction.id)

        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals("cash", entries[0].accountId)
        assertEquals(EntryDirection.DEBIT, entries[0].direction)
        assertEquals("external-income", entries[1].accountId)
        assertEquals(EntryDirection.CREDIT, entries[1].direction)
    }

    @Test
    fun addsSportExpenseUsingCashAccount() = runBlocking {
        useCase.addExpense(
            amount = Money.cnyCents(5000),
            note = "Gym",
            kind = ExpenseKind.SPORT,
            occurredAt = Instant.parse("2026-05-12T09:00:00Z")
        )

        val transaction = database.transactionDao().getRecent(1).single()
        val entries = database.ledgerEntryDao().getByTransactionId(transaction.id)

        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals("sport-expense", entries[0].accountId)
        assertEquals(EntryDirection.DEBIT, entries[0].direction)
        assertEquals("cash", entries[1].accountId)
        assertEquals(EntryDirection.CREDIT, entries[1].direction)
    }
}
