package com.bking.domain.model

enum class AccountGroup {
    ASSET,
    LIABILITY,
    INCOME,
    EXPENSE,
    EQUITY
}

enum class AccountType {
    CASH,
    BANK,
    ALIPAY,
    WECHAT,
    CREDIT_CARD,
    LOAN,
    RECEIVABLE,
    PAYABLE,
    INVESTMENT,
    INCOME_CATEGORY,
    EXPENSE_CATEGORY,
    EQUITY_ADJUSTMENT
}

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    CREDIT_CARD_EXPENSE,
    LIABILITY_REPAYMENT,
    LEND,
    BORROW,
    LOAN_PAYMENT,
    INVESTMENT_BUY,
    INVESTMENT_SELL,
    BALANCE_ADJUSTMENT
}

enum class EntryDirection {
    DEBIT,
    CREDIT
}

enum class LoanRepaymentMethod {
    EQUAL_PAYMENT,
    EQUAL_PRINCIPAL
}

enum class LoanStatus {
    ACTIVE,
    CLOSED
}
