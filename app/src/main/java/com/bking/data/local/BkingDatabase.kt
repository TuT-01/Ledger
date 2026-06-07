package com.bking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bking.data.local.dao.AccountDao
import com.bking.data.local.dao.BudgetDao
import com.bking.data.local.dao.LedgerEntryDao
import com.bking.data.local.dao.LoanDao
import com.bking.data.local.dao.LoanPaymentDao
import com.bking.data.local.dao.TransactionDao
import com.bking.data.local.entity.AccountEntity
import com.bking.data.local.entity.BudgetEntity
import com.bking.data.local.entity.LedgerEntryEntity
import com.bking.data.local.entity.LoanEntity
import com.bking.data.local.entity.LoanPaymentEntity
import com.bking.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        LedgerEntryEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        BudgetEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(BkingTypeConverters::class)
abstract class BkingDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun loanDao(): LoanDao
    abstract fun loanPaymentDao(): LoanPaymentDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS loans (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        accountId TEXT NOT NULL,
                        principalMinorUnits INTEGER NOT NULL,
                        currencyCode TEXT NOT NULL,
                        annualRateBps INTEGER NOT NULL,
                        termMonths INTEGER NOT NULL,
                        startDate TEXT NOT NULL,
                        repaymentDayOfMonth INTEGER NOT NULL,
                        repaymentMethod TEXT NOT NULL,
                        status TEXT NOT NULL,
                        openingTransactionId TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_loans_accountId ON loans(accountId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS loan_payments (
                        id TEXT NOT NULL PRIMARY KEY,
                        loanId TEXT NOT NULL,
                        transactionId TEXT NOT NULL,
                        installmentNumber INTEGER NOT NULL,
                        dueDate TEXT NOT NULL,
                        paidAt INTEGER NOT NULL,
                        principalMinorUnits INTEGER NOT NULL,
                        interestMinorUnits INTEGER NOT NULL,
                        totalMinorUnits INTEGER NOT NULL,
                        FOREIGN KEY(loanId) REFERENCES loans(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(transactionId) REFERENCES transactions(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_loan_payments_loanId_installmentNumber ON loan_payments(loanId, installmentNumber)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_loan_payments_transactionId ON loan_payments(transactionId)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id TEXT NOT NULL PRIMARY KEY,
                        month TEXT NOT NULL,
                        categoryAccountId TEXT NOT NULL,
                        amountMinorUnits INTEGER NOT NULL,
                        currencyCode TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_month_categoryAccountId ON budgets(month, categoryAccountId)"
                )
            }
        }
    }
}
