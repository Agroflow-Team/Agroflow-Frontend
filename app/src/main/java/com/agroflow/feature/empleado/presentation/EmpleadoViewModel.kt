package com.agroflow.feature.empleado.presentation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.db.DatabaseProvider
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.TaskRepository
import com.agroflow.feature.tasks.data.TaskStatus
import kotlinx.coroutines.launch

import com.agroflow.feature.inventory.data.InventoryItem
import com.agroflow.feature.inventory.data.InventoryRepository
import com.agroflow.feature.inventory.data.CreateInventoryItemRequest
import com.agroflow.feature.inventory.data.TipoItemEnum

class EmpleadoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val db = DatabaseProvider.getDatabase(application)
    private val taskRepository = TaskRepository(db.taskDao())
    private val inventoryRepository = InventoryRepository(db.inventoryDao())

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var insumos by mutableStateOf<List<InventoryItem>>(emptyList())
        private set

    var totalHorasTrabajadas by mutableStateOf(0.0)
        private set
        
    var salarioEstimado by mutableStateOf(0.0)
        private set

    var currentFincaId by mutableStateOf<String?>(null)
        private set

    var tarifaHora by mutableStateOf(0.0)
        private set

    fun loadTasks() {
        val workerId = SessionManager.userId ?: return
        viewModelScope.launch {
            var actualTrabajadorId = workerId
            
            // 1. Obtener el verdadero ID del trabajador (usando el usuarioId)
            try {
                val response = com.agroflow.core.RetrofitClient.personnelApi.getTrabajadores()
                if (response.isSuccessful) {
                    val trabajador = response.body()?.find { it.usuarioId == workerId || it.id == workerId }
                    if (trabajador != null) {
                        actualTrabajadorId = trabajador.id
                        tarifaHora = trabajador.tarifaHora
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Ahora sí, cargar las tareas usando el ID correcto
            tasks = taskRepository.getTasksByWorker(actualTrabajadorId)
            
            totalHorasTrabajadas = tasks.filter { it.estado == TaskStatus.COMPLETADA }
                .sumOf { it.horasEfectivas }
            
            salarioEstimado = totalHorasTrabajadas * tarifaHora
            
            // Obtener fincaId: primero de las tareas, si no hay, del API de fincas
            var fincaId = tasks.firstOrNull()?.fincaId
            
            if (fincaId == null) {
                try {
                    val response = com.agroflow.core.RetrofitClient.personnelApi.getFincas()
                    if (response.isSuccessful) {
                        fincaId = response.body()?.firstOrNull()?.id
                    }
                } catch (_: Exception) {}
            }
            
            currentFincaId = fincaId
            
            if (fincaId != null) {
                loadInventory(fincaId)
            }
        }
    }

    fun loadInventory(fincaId: String) {
        viewModelScope.launch {
            insumos = inventoryRepository.getAllByFinca(fincaId)
        }
    }

    fun updateTaskProgress(taskId: String, nuevasHoras: Double, novedades: String, severidad: String?, estado: TaskStatus, onComplete: () -> Unit) {
        val workerId = SessionManager.userId ?: return
        viewModelScope.launch {
            val req = com.agroflow.feature.tasks.data.UpdateProgressRequest(
                trabajadorId = workerId,
                nuevasHoras = nuevasHoras,
                novedades = novedades,
                severidadNovedad = severidad,
                nuevoEstado = estado
            )
            // Ideally we'd hit retrofit client here too for sync.
            try {
                com.agroflow.core.RetrofitClient.taskApi.updateProgress(taskId, req)
            } catch (e: Exception) { }

            taskRepository.updateTaskStatus(taskId, workerId, nuevasHoras, novedades, estado)
            loadTasks()
            onComplete()
        }
    }

    fun addInsumo(fincaId: String, nombre: String, cantidad: Double, unidad: String, onComplete: () -> Unit) {
        val workerId = SessionManager.userId ?: return
        val req = CreateInventoryItemRequest(
            fincaId = fincaId,
            registradoPorTrabajadorId = workerId,
            nombreItem = nombre,
            tipo = TipoItemEnum.INSUMO,
            cantidad = cantidad,
            unidadMedida = unidad,
            costoUnitario = 0.0
        )
        viewModelScope.launch {
            inventoryRepository.addItem(req) {
                // Recargar inventario después de agregar
            }
            loadInventory(fincaId)
            onComplete()
        }
    }

    fun updateInsumoStock(itemId: String, cantidadUsada: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            inventoryRepository.updateStock(itemId, cantidadUsada) {
                currentFincaId?.let { loadInventory(it) }
                onComplete()
            }
        }
    }

    fun editInsumo(itemId: String, nombre: String, cantidad: Double, unidad: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            inventoryRepository.editItem(itemId, nombre, cantidad, unidad) {
                currentFincaId?.let { loadInventory(it) }
                onComplete()
            }
        }
    }
}
