package com.agroflow.feature.inventory.data

import com.agroflow.core.RetrofitClient
import com.agroflow.feature.inventory.data.local.InventoryDao
import com.agroflow.feature.inventory.data.local.InventoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class InventoryRepository(private val inventoryDao: InventoryDao) {

    suspend fun getInsumosByFinca(fincaId: String): List<InventoryItem> {
        return withContext(Dispatchers.IO) {
            syncPendingItems()
            fetchFromServer(fincaId)
            inventoryDao.getInventoryByFincaAndType(fincaId, "INSUMO").map { it.toDomain() }
        }
    }

    suspend fun getAllByFinca(fincaId: String): List<InventoryItem> {
        return withContext(Dispatchers.IO) {
            syncPendingItems()
            fetchFromServer(fincaId)
            inventoryDao.getInventoryByFinca(fincaId).map { it.toDomain() }
        }
    }

    private suspend fun fetchFromServer(fincaId: String) {
        try {
            val response = RetrofitClient.inventoryApi.getInventoryByFinca(fincaId)
            if (response.isSuccessful) {
                val serverItems = response.body() ?: emptyList()
                val pendingIds = inventoryDao.getPendingSyncItems().map { it.id }
                val entities = serverItems.map { it.toEntity() }.filter { it.id !in pendingIds }
                inventoryDao.insertItems(entities)
            }
        } catch (e: Exception) {
            // Silencioso si no hay red
        }
    }

    suspend fun addItem(item: CreateInventoryItemRequest, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            val localId = UUID.randomUUID().toString()
            val entity = InventoryEntity(
                id = localId,
                fincaId = item.fincaId,
                registradoPorTrabajadorId = item.registradoPorTrabajadorId,
                nombreItem = item.nombreItem,
                tipo = item.tipo.name,
                cantidad = item.cantidad,
                unidadMedida = item.unidadMedida,
                costoUnitario = item.costoUnitario ?: 0.0,
                eliminado = false,
                isSyncPending = true,
                syncAction = "CREATE"
            )
            inventoryDao.insertItem(entity)

            try {
                val response = RetrofitClient.inventoryApi.addItem(item)
                if (response.isSuccessful) {
                    inventoryDao.markAsSynced(localId)
                }
            } catch (e: Exception) {}
            onComplete()
        }
    }

    suspend fun updateStock(itemId: String, cantidadUsada: Double, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            inventoryDao.discountStock(itemId, cantidadUsada)
            
            try {
                val req = UpdateStockRequest(cantidadUsada)
                val response = RetrofitClient.inventoryApi.updateStock(itemId, req)
                if (response.isSuccessful) {
                    inventoryDao.markAsSynced(itemId)
                }
            } catch (e: Exception) {
                // Ignore
            }
            onComplete()
        }
    }

    suspend fun editItem(itemId: String, nombre: String, cantidad: Double, unidad: String, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            val entity = inventoryDao.getItemById(itemId) ?: return@withContext
            val updated = entity.copy(
                nombreItem = nombre,
                cantidad = cantidad,
                unidadMedida = unidad,
                isSyncPending = true,
                syncAction = "UPDATE"
            )
            inventoryDao.insertItem(updated)
            
            try {
                val req = com.agroflow.feature.inventory.data.UpdateInventoryItemRequest(nombre, cantidad, unidad)
                val response = RetrofitClient.inventoryApi.editItem(itemId, req)
                if (response.isSuccessful) {
                    inventoryDao.markAsSynced(itemId)
                }
            } catch (e: Exception) {
                // Se mantiene pendiente de sync
            }
            onComplete()
        }
    }

    private suspend fun syncPendingItems() {
        val pending = inventoryDao.getPendingSyncItems()
        for (entity in pending) {
            try {
                if (entity.syncAction == "CREATE") {
                    val request = CreateInventoryItemRequest(
                        fincaId = entity.fincaId,
                        registradoPorTrabajadorId = entity.registradoPorTrabajadorId,
                        nombreItem = entity.nombreItem,
                        tipo = TipoItemEnum.valueOf(entity.tipo),
                        cantidad = entity.cantidad,
                        unidadMedida = entity.unidadMedida,
                        costoUnitario = entity.costoUnitario
                    )
                    val response = RetrofitClient.inventoryApi.addItem(request)
                    if (response.isSuccessful) {
                        inventoryDao.markAsSynced(entity.id)
                    }
                } else if (entity.syncAction == "UPDATE") {
                    inventoryDao.markAsSynced(entity.id)
                }
            } catch (e: Exception) {}
        }
    }

    private fun InventoryItem.toEntity(): InventoryEntity {
        return InventoryEntity(
            id = this.id ?: "",
            fincaId = this.fincaId,
            registradoPorTrabajadorId = this.registradoPorTrabajadorId,
            nombreItem = this.nombreItem,
            tipo = this.tipo.name,
            cantidad = this.cantidad,
            unidadMedida = this.unidadMedida,
            eliminado = this.eliminado,
            costoUnitario = this.costoUnitario ?: 0.0,
            isSyncPending = false,
            syncAction = "NONE"
        )
    }

    private fun InventoryEntity.toDomain(): InventoryItem {
        return InventoryItem(
            id = this.id,
            fincaId = this.fincaId,
            registradoPorTrabajadorId = this.registradoPorTrabajadorId,
            nombreItem = this.nombreItem,
            tipo = TipoItemEnum.valueOf(this.tipo),
            cantidad = this.cantidad,
            unidadMedida = this.unidadMedida,
            fechaActualizacion = null,
            eliminado = this.eliminado,
            costoUnitario = this.costoUnitario,
            estadoSincronizacion = "PENDIENTE" // Sólo para display si es necesario
        )
    }
}
