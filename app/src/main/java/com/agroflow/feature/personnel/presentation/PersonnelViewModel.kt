package com.agroflow.feature.personnel.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.personnel.data.Finca
import com.agroflow.feature.personnel.data.Trabajador
import kotlinx.coroutines.launch

sealed class PersonnelUiState {
    object Loading : PersonnelUiState()
    object Success : PersonnelUiState()
    data class Error(val message: String) : PersonnelUiState()
}

class PersonnelViewModel : ViewModel() {
    var uiState by mutableStateOf<PersonnelUiState>(PersonnelUiState.Loading)
        private set

    var fincas by mutableStateOf<List<Finca>>(emptyList())
        private set
        
    var trabajadores by mutableStateOf<List<Trabajador>>(emptyList())
        private set

    // Selection state to keep context for other screens
    var selectedFinca by mutableStateOf<Finca?>(null)
    var selectedTrabajador by mutableStateOf<Trabajador?>(null)

    fun loadFincas() {
        viewModelScope.launch {
            uiState = PersonnelUiState.Loading
            try {
                val response = RetrofitClient.personnelApi.getFincas()
                if (response.isSuccessful) {
                    fincas = response.body() ?: emptyList()
                    uiState = PersonnelUiState.Success
                } else {
                    uiState = PersonnelUiState.Error("Error al cargar fincas: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = PersonnelUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun createFinca(nombre: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.personnelApi.createFinca(com.agroflow.feature.personnel.data.CreateFincaRequest(nombre))
                if (response.isSuccessful) {
                    loadFincas()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadTrabajadores(fincaId: String? = null) {
        viewModelScope.launch {
            uiState = PersonnelUiState.Loading
            try {
                val response = if (fincaId != null) {
                    RetrofitClient.personnelApi.getTrabajadoresByFinca(fincaId)
                } else {
                    RetrofitClient.personnelApi.getTrabajadores()
                }
                
                if (response.isSuccessful) {
                    trabajadores = response.body() ?: emptyList()
                    uiState = PersonnelUiState.Success
                } else {
                    uiState = PersonnelUiState.Error("Error al cargar trabajadores: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = PersonnelUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun createTrabajador(fincaId: String, nombreCompleto: String, documento: String, tarifaHora: Double, correo: String, clave: String) {
        viewModelScope.launch {
            try {
                val request = com.agroflow.feature.personnel.data.CreateTrabajadorRequest(fincaId, nombreCompleto, documento, tarifaHora, correo, clave)
                val response = RetrofitClient.personnelApi.createTrabajador(request)
                if (response.isSuccessful) {
                    selectedFinca?.let { loadTrabajadores(it.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createUser(nombre: String, correo: String, clave: String, rolId: String) {
        viewModelScope.launch {
            try {
                val request = com.agroflow.feature.auth.data.CreateUserRequest(nombre, correo, clave, rolId)
                val response = RetrofitClient.userApi.createAgricultor(request)
                if (response.isSuccessful) {
                    selectedFinca?.let { loadTrabajadores(it.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTrabajador(id: String, nombreCompleto: String, documento: String, tarifaHora: Double) {
        viewModelScope.launch {
            try {
                val request = com.agroflow.feature.personnel.data.UpdateTrabajadorRequest(nombreCompleto, documento, tarifaHora)
                val response = RetrofitClient.personnelApi.updateTrabajador(id, request)
                if (response.isSuccessful) {
                    selectedFinca?.let { loadTrabajadores(it.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTrabajador(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.personnelApi.deleteTrabajador(id)
                if (response.isSuccessful) {
                    selectedFinca?.let { loadTrabajadores(it.id) }
                    if (selectedTrabajador?.id == id) {
                        selectedTrabajador = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
