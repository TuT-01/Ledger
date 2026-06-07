package com.bking.data.local.model

import com.bking.domain.model.EntryDirection
import java.time.Instant

data class LedgerEntryWithTransaction(
    val id: String,
    val transactionId: String,
    val accountId: String,
    val direction: EntryDirection,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val transactionOccurredAt: Instant
)

