package com.bking.data.di

import android.content.Context
import androidx.room.Room
import com.bking.data.local.BkingDatabase
import com.bking.data.local.dao.AccountDao
import com.bking.data.local.dao.BudgetDao
import com.bking.data.local.dao.LedgerEntryDao
import com.bking.data.local.dao.LoanDao
import com.bking.data.local.dao.LoanPaymentDao
import com.bking.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BkingDatabase = Room.databaseBuilder(
        context,
        BkingDatabase::class.java,
        "bking.db"
    )
        .addMigrations(BkingDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideAccountDao(database: BkingDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: BkingDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideLedgerEntryDao(database: BkingDatabase): LedgerEntryDao = database.ledgerEntryDao()

    @Provides
    fun provideLoanDao(database: BkingDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideLoanPaymentDao(database: BkingDatabase): LoanPaymentDao = database.loanPaymentDao()

    @Provides
    fun provideBudgetDao(database: BkingDatabase): BudgetDao = database.budgetDao()
}
