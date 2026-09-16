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

    var currentFincaNombre by mutableStateOf<String?>(null)
        private set

    var nombreTrabajador by mutableStateOf<String?>(null)
        private set

    var tarifaHora by mutableStateOf(0.0)
        private set

    var actualTrabajadorId by mutableStateOf<String?>(null)
        private set

    var lastError by mutableStateOf<String?>(null)
        private set

    fun loadTasks() {
        val workerId = SessionManager.userId ?: return
        viewModelScope.launch {
            var targetTrabajadorId = workerId
            
            // 1. Obtener el verdadero perfil del trabajador (usando el usuarioId o id)
            try {
                val response = com.agroflow.core.RetrofitClient.personnelApi.getTrabajadores()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    val trabajador = lista.find { 
                        it.usuarioId.equals(workerId, ignoreCase = true) || it.id.equals(workerId, ignoreCase = true) 
                    }
                    if (trabajador != null) {
                        targetTrabajadorId = trabajador.id
                        actualTrabajadorId = trabajador.id
                        tarifaHora = trabajador.tarifaHora
                        nombreTrabajador = trabajador.nombreCompleto
                        currentFincaId = trabajador.fincaId
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EmpleadoVM", "Error al consultar trabajadores: ${e.message}")
            }

            // 2. Obtener el nombre de la finca asignada
            val fincaIdToSearch = currentFincaId
            try {
                val responseFincas = com.agroflow.core.RetrofitClient.personnelApi.getFincas()
                if (responseFincas.isSuccessful) {
                    val fincasList = responseFincas.body() ?: emptyList()
                    if (fincaIdToSearch != null) {
                        val fincaEncontrada = fincasList.find { it.id.equals(fincaIdToSearch, ignoreCase = true) }
                        if (fincaEncontrada != null) {
                            currentFincaNombre = fincaEncontrada.nombre
                        }
                    } else if (fincasList.isNotEmpty()) {
                        currentFincaId = fincasList.first().id
                        currentFincaNombre = fincasList.first().nombre
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EmpleadoVM", "Error al consultar fincas: ${e.message}")
            }

            try {
                // 3. Cargar las tareas usando el ID correcto desde el Repositorio (Room + API)
                val fetchedTasks = taskRepository.getTasksByWorker(targetTrabajadorId)
                
                if (fetchedTasks.isEmpty() && targetTrabajadorId != workerId) {
                    val fallback = taskRepository.getTasksByWorker(workerId)
                    tasks = if (fallback.isNotEmpty()) fallback else fetchedTasks
                } else {
                    tasks = fetchedTasks
                }
                
                // 4. Si TODAVA est vaco, hagamos un fallback conectndonos directo al Finca API para ver si las tareas se crearon mal
                if (tasks.isEmpty() && currentFincaId != null) {
                    val response = com.agroflow.core.RetrofitClient.taskApi.getTasksByFinca(currentFincaId!!)
                    if (response.isSuccessful) {
                        val allFincaTasks = response.body() ?: emptyList()
                        // Filtramos las que le pertenezcan al usuario
                        val myTasks = allFincaTasks.filter { 
                            it.trabajadorId.equals(targetTrabajadorId, ignoreCase = true) || 
                            it.trabajadorId.equals(workerId, ignoreCase = true)
                        }
                        if (myTasks.isNotEmpty()) {
                            tasks = myTasks
                            lastError = "Tareas recuperadas desde la nube Finca API."
                        } else if (allFincaTasks.isNotEmpty()) {
                            // S hay tareas en la finca, pero con OTRO ID de trabajador!
                            lastError = "Hay tareas en la finca, pero estn asignadas a otro ID. (Tu ID: $targetTrabajadorId)"
                        } else {
                            lastError = "No hay tareas en la finca ni para ti. (ID: $targetTrabajadorId)"
                        }
                    }
                } else if (tasks.isEmpty()) {
                    lastError = "No se encontraron tareas para el trabajador (ID: $targetTrabajadorId)"
                } else {
                    lastError = null
                }
            } catch (e: Exception) {
                lastError = "Error loading tasks: ${e.message}"
            }

            totalHorasTrabajadas = tasks.filter { it.estado == TaskStatus.COMPLETADA }
                .sumOf { it.horasEfectivas }
            
            salarioEstimado = totalHorasTrabajadas * tarifaHora
            
            val finalFincaId = currentFincaId ?: tasks.firstOrNull()?.fincaId
            if (finalFincaId != null) {
                loadInventory(finalFincaId)
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
