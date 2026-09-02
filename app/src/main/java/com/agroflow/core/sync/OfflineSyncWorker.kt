package com.agroflow.core.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OfflineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("OfflineSyncWorker", "Iniciando sincronizacion en segundo plano...")
            val db = com.agroflow.core.db.DatabaseProvider.getDatabase(applicationContext)
            val inventoryRepository = com.agroflow.feature.inventory.data.InventoryRepository(db.inventoryDao())
            
            // Sincronizar todos los items pendientes de inventario
            inventoryRepository.syncPendingItems()
            Log.d("OfflineSyncWorker", "Sincronizacion completada con exito")
            
            Result.success()
        } catch (e: Exception) {
            Log.e("OfflineSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}
