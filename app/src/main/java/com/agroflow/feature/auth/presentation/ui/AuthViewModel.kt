package com.agroflow.feature.auth.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.auth.data.LoginRequest
import kotlinx.coroutines.launch

// Definimos los posibles estados de la pantalla
sealed class LoginUiState {
    object Idle : LoginUiState() // Estado inicial (esperando)
    object Loading : LoginUiState() // Cargando peticion
    object Success : LoginUiState() // Login exitoso
    data class Error(val message: String) : LoginUiState() // Fallo el login
}

class AuthViewModel : ViewModel() {

    // Variable que la pantalla observara para reaccionar
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun login(correo: String, clave: String) {
        viewModelScope.launch {
            uiState = LoginUiState.Loading // Cambiamos la UI a "Cargando"
            Log.d("AgroFlowLogin", "Intentando enviar peticion a Spring Boot...")

            try {
                val request = LoginRequest(correo, clave)
                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("AgroFlowLogin", "Exito Token recibido: ${body?.token}")
                    if (body != null) {
                        com.agroflow.core.session.SessionManager.saveSession(
                            userId = body.usuarioId,
                            email = body.correo,
                            roleId = body.rolId,
                            token = body.token
                        )
                    }
                    uiState = LoginUiState.Success // Cambiamos la UI a "Exito"
                } else {
                    Log.e("AgroFlowLogin", "Error del servidor: ${response.code()}")
                    uiState = LoginUiState.Error("Credenciales incorrectas o error del servidor")
                }
            } catch (e: Exception) {
                Log.e("AgroFlowLogin", "Fallo la conexion: ${e.message}")
                uiState = LoginUiState.Error("Error de conexion. Revisa tu red.")
            }
        }
    }

    fun registerCliente(nombre: String, correo: String, clave: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.agroflow.feature.auth.data.CreateUserRequest(nombre, correo, clave, com.agroflow.core.session.SessionManager.ROLE_CLIENTE)
                val response = RetrofitClient.userApi.createCliente(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error al registrar: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }
}