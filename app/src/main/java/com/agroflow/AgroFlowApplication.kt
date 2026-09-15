package com.agroflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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
        createNotificationChannel()
        setupOfflineSync()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "agroflow_tasks_channel"
            val channelName = "AgroFlow - Tareas y Alertas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notificaciones de asignación y avances de tareas agrícolas"
                enableLights(true)
                lightColor = 0xFF2E7D32.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
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
