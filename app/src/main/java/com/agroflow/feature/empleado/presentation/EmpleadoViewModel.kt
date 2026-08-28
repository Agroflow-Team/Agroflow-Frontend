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

    fun loadTasks() {
        val workerId = SessionManager.userId ?: return
        viewModelScope.launch {
            tasks = taskRepository.getTasksByWorker(workerId)
            
            totalHorasTrabajadas = tasks.filter { it.estado == TaskStatus.COMPLETADA }
                .sumOf { it.horasReales ?: 0.0 }
                
            val tarifaHora = 5000.0 
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

    fun updateTaskProgress(taskId: String, nuevasHoras: Double, novedades: String, estado: TaskStatus, onComplete: () -> Unit) {
        val workerId = SessionManager.userId ?: return
        viewModelScope.launch {
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
