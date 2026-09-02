package com.agroflow.feature.tasks.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.tasks.data.CreateTaskRequest
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.UpdateProgressRequest
import kotlinx.coroutines.launch

sealed class TaskUiState {
    object Idle : TaskUiState()
    object Loading : TaskUiState()
    object Success : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}

class TaskViewModel : ViewModel() {
    var uiState by mutableStateOf<TaskUiState>(TaskUiState.Idle)
        private set

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    fun loadTasksForWorker(workerId: String) {
        viewModelScope.launch {
            uiState = TaskUiState.Loading
            try {
                val response = RetrofitClient.taskApi.getTasksByWorker(workerId)
                if (response.isSuccessful) {
                    tasks = response.body() ?: emptyList()
                    uiState = TaskUiState.Success
                } else {
                    uiState = TaskUiState.Error("Error al cargar tareas: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = TaskUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun loadTasksByFinca(fincaId: String) {
        viewModelScope.launch {
            uiState = TaskUiState.Loading
            try {
                val response = RetrofitClient.taskApi.getTasksByFinca(fincaId)
                if (response.isSuccessful) {
                    tasks = response.body() ?: emptyList()
                    uiState = TaskUiState.Success
                } else {
                    uiState = TaskUiState.Error("Error al cargar tareas: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = TaskUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun createTask(request: CreateTaskRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = TaskUiState.Loading
            try {
                val response = RetrofitClient.taskApi.createTask(request)
                if (response.isSuccessful) {
                    uiState = TaskUiState.Success
                    loadTasksForWorker(request.trabajadorId)
                    onComplete()
                } else {
                    uiState = TaskUiState.Error("Error al crear tarea: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = TaskUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun updateProgress(taskId: String, request: UpdateProgressRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = TaskUiState.Loading
            try {
                val response = RetrofitClient.taskApi.updateProgress(taskId, request)
                if (response.isSuccessful) {
                    uiState = TaskUiState.Success
                    loadTasksForWorker(request.trabajadorId)
                    onComplete()
                } else {
                    uiState = TaskUiState.Error("Error al actualizar tarea: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = TaskUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
