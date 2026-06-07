package com.bking.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bking.data.local.BkingDatabase
import com.bking.domain.model.AccountGroup
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountSeedRepositoryTest {
    private lateinit var database: BkingDatabase
    private lateinit var repository: AccountSeedRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BkingDatabase::class.java
        ).build()
        repository = AccountSeedRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedsDefaultAccountsWhenDatabaseIsEmpty() = runBlocking {
        repository.seedDefaultsIfNeeded()

        val accounts = database.accountDao().getAll()
        val ids = accounts.map { it.id }.toSet()

        assertTrue("cash" in ids)
        assertTrue("salary-income" in ids)
        assertTrue("external-income" in ids)
        assertTrue("other-income" in ids)
        assertTrue("meal-expense" in ids)
        assertTrue("shopping-expense" in ids)
        assertTrue("sport-expense" in ids)
        assertTrue("other-expense" in ids)
        assertEquals(AccountGroup.ASSET, accounts.first { it.id == "cash" }.group)
        assertEquals(AccountGroup.INCOME, accounts.first { it.id == "salary-income" }.group)
        assertEquals(AccountGroup.INCOME, accounts.first { it.id == "other-income" }.group)
        assertEquals(AccountGroup.EXPENSE, accounts.first { it.id == "meal-expense" }.group)
        assertEquals(AccountGroup.EXPENSE, accounts.first { it.id == "other-expense" }.group)
    }

    @Test
    fun seedIsIdempotent() = runBlocking {
        repository.seedDefaultsIfNeeded()
        repository.seedDefaultsIfNeeded()

        val accounts = database.accountDao().getAll()

        assertEquals(DefaultAccounts.items.size, accounts.size)
    }
}
