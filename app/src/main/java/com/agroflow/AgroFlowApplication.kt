package com.agroflow

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.agroflow.core.sync.OfflineSyncWorker
import java.util.concurrent.TimeUnit

class AgroFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupOfflineSync()
    }

    private fun setupOfflineSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "OfflineSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
