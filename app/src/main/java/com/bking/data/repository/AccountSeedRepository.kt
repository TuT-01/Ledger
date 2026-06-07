package com.bking.data.repository

import com.bking.data.local.BkingDatabase
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountSeedRepository @Inject constructor(
    private val database: BkingDatabase
) {
    suspend fun seedDefaultsIfNeeded(now: Instant = Instant.now()) {
        val accountDao = database.accountDao()
        val existingIds = accountDao.getAll().map { it.id }.toSet()
        val missingDefaults = DefaultAccounts.create(now)
            .filterNot { it.id in existingIds }

        if (missingDefaults.isNotEmpty()) {
            accountDao.insertAllIgnore(missingDefaults)
        }
    }
}
