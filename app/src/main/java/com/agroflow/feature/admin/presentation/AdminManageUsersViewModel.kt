package com.agroflow.feature.admin.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.auth.data.CreateUserRequest
import kotlinx.coroutines.launch

sealed class ManageUsersUiState {
    object Idle : ManageUsersUiState()
    object Loading : ManageUsersUiState()
    object Success : ManageUsersUiState()
    data class Error(val message: String) : ManageUsersUiState()
}

class AdminManageUsersViewModel : ViewModel() {
    var uiState by mutableStateOf<ManageUsersUiState>(ManageUsersUiState.Idle)
        private set

    fun createUser(nombre: String, correo: String, contrasena: String, rol: String) {
        viewModelScope.launch {
            uiState = ManageUsersUiState.Loading
            try {
                // Role handling according to the backend
                val roleId = if (rol == "Administrador") {
                    com.agroflow.core.session.SessionManager.ROLE_ADMIN
                } else {
                    com.agroflow.core.session.SessionManager.ROLE_AGRICULTOR
                }
                val request = CreateUserRequest(nombre, correo, contrasena, roleId)
                val response = RetrofitClient.userApi.createAdminUser(request)

                if (response.isSuccessful) {
                    uiState = ManageUsersUiState.Success
                } else {
                    uiState = ManageUsersUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = ManageUsersUiState.Error("Error de red: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        uiState = ManageUsersUiState.Idle
    }
}
