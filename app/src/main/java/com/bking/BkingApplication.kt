package com.bking

import android.app.Application
import com.bking.data.repository.AccountSeedRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class BkingApplication : Application() {
    @Inject
    lateinit var accountSeedRepository: AccountSeedRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            accountSeedRepository.seedDefaultsIfNeeded()
        }
    }
}
