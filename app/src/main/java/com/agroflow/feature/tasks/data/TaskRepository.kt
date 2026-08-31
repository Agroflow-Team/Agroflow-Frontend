package com.agroflow.feature.tasks.data

import com.agroflow.core.RetrofitClient
import com.agroflow.feature.tasks.data.local.TaskDao
import com.agroflow.feature.tasks.data.local.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getTasksByWorker(workerId: String): List<Task> {
        return withContext(Dispatchers.IO) {
            // Sincronizar tareas pendientes hacia el servidor (opcional en un MVP offline-first simple, pero buena práctica)
            syncPendingTasks()

            // 1. Intentar obtener del servidor
            try {
                val response = RetrofitClient.taskApi.getTasksByWorker(workerId)
                if (response.isSuccessful) {
                    val serverTasks = response.body() ?: emptyList()
                    val pendingIds = taskDao.getPendingSyncTasks().map { it.id }
                    
                    val entities = serverTasks.map { it.toEntity() }.filter { it.id !in pendingIds }
                    taskDao.insertTasks(entities)
                }
            } catch (e: Exception) {
                // Si falla (por ej. offline), ignoramos y devolvemos lo que hay en local
            }

            // 3. Obtener siempre la fuente de verdad: la BD local
            taskDao.getTasksByWorker(workerId).map { it.toDomain() }
        }
    }

    suspend fun updateTaskStatus(taskId: String, workerId: String, nuevasHoras: Double, novedades: String, nuevoEstado: TaskStatus) {
        withContext(Dispatchers.IO) {
            // 1. Guardar localmente y marcar como pendiente de sincronización
            taskDao.updateProgress(taskId, nuevoEstado.name, nuevasHoras, novedades)

            // 2. Intentar subir al servidor inmediatamente
            try {
                val request = UpdateProgressRequest(workerId, nuevasHoras, novedades, null, nuevoEstado)
                val response = RetrofitClient.taskApi.updateProgress(taskId, request)
                if (response.isSuccessful) {
                    // 3. Si tuvo éxito, desmarcamos de pendiente
                    taskDao.markAsSynced(taskId)
                }
            } catch (e: Exception) {
                // Falla silenciosamente, la tarea se sincronizará luego gracias a isSyncPending = 1
            }
        }
    }

    private suspend fun syncPendingTasks() {
        // En una app real, esto iría en un WorkManager
        val pendingTasks = taskDao.getPendingSyncTasks()
        for (entity in pendingTasks) {
            try {
                val request = UpdateProgressRequest(
                    trabajadorId = entity.trabajadorId,
                    nuevasHoras = entity.horasReales,
                    novedades = entity.novedades ?: "",
                    nuevoEstado = TaskStatus.valueOf(entity.estado)
                )
                val response = RetrofitClient.taskApi.updateProgress(entity.id, request)
                if (response.isSuccessful) {
                    taskDao.markAsSynced(entity.id)
                }
            } catch (e: Exception) {
                // Falla y se queda para el siguiente intento
            }
        }
    }

    // --- Mappers ---
    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = this.id ?: "",
            fincaId = this.fincaId,
            trabajadorId = this.trabajadorId,
            titulo = this.titulo,
            descripcion = this.descripcion,
            estado = this.estado.name,
            novedades = this.novedades,
            horasEstimadas = this.horasEstimadas ?: 0.0,
            horasReales = this.horasReales ?: 0.0,
            isSyncPending = false
        )
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = this.id,
            fincaId = this.fincaId,
            trabajadorId = this.trabajadorId,
            titulo = this.titulo,
            descripcion = this.descripcion ?: "",
            estado = TaskStatus.valueOf(this.estado),
            novedades = this.novedades,
            horasEstimadas = this.horasEstimadas,
            horasReales = this.horasReales
        )
    }
}
