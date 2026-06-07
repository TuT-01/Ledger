package com.bking.domain.service

import com.bking.domain.model.EntryDirection
import com.bking.domain.model.LedgerEntryDraft
import com.bking.domain.model.Money
import com.bking.domain.model.TransactionType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleEntryFactoryTest {
    private val occurredAt = Instant.parse("2026-05-12T10:00:00Z")

    @Test
    fun `expense debits expense account and credits asset account`() {
        val draft = DoubleEntryFactory.expense(
            amount = Money.cnyCents(1800),
            paidFromAccountId = "bank",
            expenseAccountId = "food",
            occurredAt = occurredAt,
            note = "Lunch"
        )

        assertEquals(TransactionType.EXPENSE, draft.type)
        assertEquals("Lunch", draft.note)
        assertEquals(
            listOf(
                LedgerEntryDraft("food", EntryDirection.DEBIT, Money.cnyCents(1800)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(1800))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `income debits receiving account and credits income account`() {
        val draft = DoubleEntryFactory.income(
            amount = Money.cnyCents(880000),
            receivedToAccountId = "bank",
            incomeAccountId = "salary",
            occurredAt = occurredAt,
            note = "Salary"
        )

        assertEquals(TransactionType.INCOME, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("bank", EntryDirection.DEBIT, Money.cnyCents(880000)),
                LedgerEntryDraft("salary", EntryDirection.CREDIT, Money.cnyCents(880000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `transfer debits target account and credits source account`() {
        val draft = DoubleEntryFactory.transfer(
            amount = Money.cnyCents(50000),
            fromAccountId = "bank",
            toAccountId = "wechat",
            occurredAt = occurredAt,
            note = "Move cash"
        )

        assertEquals(TransactionType.TRANSFER, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("wechat", EntryDirection.DEBIT, Money.cnyCents(50000)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(50000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `credit card spending debits expense and credits liability`() {
        val draft = DoubleEntryFactory.creditCardExpense(
            amount = Money.cnyCents(12900),
            creditCardAccountId = "credit-card",
            expenseAccountId = "shopping",
            occurredAt = occurredAt,
            note = "Shoes"
        )

        assertEquals(TransactionType.CREDIT_CARD_EXPENSE, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("shopping", EntryDirection.DEBIT, Money.cnyCents(12900)),
                LedgerEntryDraft("credit-card", EntryDirection.CREDIT, Money.cnyCents(12900))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `credit card repayment debits liability and credits paying asset`() {
        val draft = DoubleEntryFactory.repayLiability(
            amount = Money.cnyCents(30000),
            paidFromAccountId = "bank",
            liabilityAccountId = "credit-card",
            occurredAt = occurredAt,
            note = "Card repayment"
        )

        assertEquals(TransactionType.LIABILITY_REPAYMENT, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("credit-card", EntryDirection.DEBIT, Money.cnyCents(30000)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(30000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `loan payment debits liability principal and interest expense then credits paying asset`() {
        val draft = DoubleEntryFactory.loanPayment(
            principal = Money.cnyCents(200000),
            interest = Money.cnyCents(32000),
            paidFromAccountId = "bank",
            liabilityAccountId = "mortgage-loan",
            interestExpenseAccountId = "loan-interest-expense",
            occurredAt = occurredAt,
            note = "Mortgage payment"
        )

        assertEquals(TransactionType.LOAN_PAYMENT, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("mortgage-loan", EntryDirection.DEBIT, Money.cnyCents(200000)),
                LedgerEntryDraft("loan-interest-expense", EntryDirection.DEBIT, Money.cnyCents(32000)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(232000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `lending money debits receivable and credits cash asset`() {
        val draft = DoubleEntryFactory.lend(
            amount = Money.cnyCents(200000),
            paidFromAccountId = "bank",
            receivableAccountId = "friend-a",
            occurredAt = occurredAt,
            note = "Loan to friend"
        )

        assertEquals(TransactionType.LEND, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("friend-a", EntryDirection.DEBIT, Money.cnyCents(200000)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(200000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `borrowing money debits cash asset and credits payable liability`() {
        val draft = DoubleEntryFactory.borrow(
            amount = Money.cnyCents(100000),
            receivedToAccountId = "bank",
            liabilityAccountId = "family-loan",
            occurredAt = occurredAt,
            note = "Borrow from family"
        )

        assertEquals(TransactionType.BORROW, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("bank", EntryDirection.DEBIT, Money.cnyCents(100000)),
                LedgerEntryDraft("family-loan", EntryDirection.CREDIT, Money.cnyCents(100000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `investment buy debits investment asset and credits funding asset`() {
        val draft = DoubleEntryFactory.investmentBuy(
            amount = Money.cnyCents(1000000),
            paidFromAccountId = "bank",
            investmentAccountId = "fund",
            occurredAt = occurredAt,
            note = "Buy fund"
        )

        assertEquals(TransactionType.INVESTMENT_BUY, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("fund", EntryDirection.DEBIT, Money.cnyCents(1000000)),
                LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(1000000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `investment sell debits receiving asset and credits investment asset`() {
        val draft = DoubleEntryFactory.investmentSell(
            amount = Money.cnyCents(980000),
            receivedToAccountId = "bank",
            investmentAccountId = "fund",
            occurredAt = occurredAt,
            note = "Sell fund"
        )

        assertEquals(TransactionType.INVESTMENT_SELL, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("bank", EntryDirection.DEBIT, Money.cnyCents(980000)),
                LedgerEntryDraft("fund", EntryDirection.CREDIT, Money.cnyCents(980000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `opening asset balance debits asset and credits equity`() {
        val draft = DoubleEntryFactory.openingAssetBalance(
            amount = Money.cnyCents(1200000),
            assetAccountId = "bank",
            equityAccountId = "opening-equity",
            occurredAt = occurredAt,
            note = "Initial bank balance"
        )

        assertEquals(TransactionType.BALANCE_ADJUSTMENT, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("bank", EntryDirection.DEBIT, Money.cnyCents(1200000)),
                LedgerEntryDraft("opening-equity", EntryDirection.CREDIT, Money.cnyCents(1200000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }

    @Test
    fun `opening liability balance debits equity and credits liability`() {
        val draft = DoubleEntryFactory.openingLiabilityBalance(
            amount = Money.cnyCents(450000),
            liabilityAccountId = "credit-card",
            equityAccountId = "opening-equity",
            occurredAt = occurredAt,
            note = "Initial card balance"
        )

        assertEquals(TransactionType.BALANCE_ADJUSTMENT, draft.type)
        assertEquals(
            listOf(
                LedgerEntryDraft("opening-equity", EntryDirection.DEBIT, Money.cnyCents(450000)),
                LedgerEntryDraft("credit-card", EntryDirection.CREDIT, Money.cnyCents(450000))
            ),
            draft.entries
        )
        assertTrue(draft.isBalanced())
    }
}
