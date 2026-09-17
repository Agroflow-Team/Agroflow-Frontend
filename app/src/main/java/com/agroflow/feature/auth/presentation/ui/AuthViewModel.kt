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
import kotlinx.coroutines.tasks.await

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

    fun resetState() {
        uiState = LoginUiState.Idle
    }

    fun login(correo: String, clave: String) {
        viewModelScope.launch {
            uiState = LoginUiState.Loading
            Log.d("AgroFlowLogin", "Intentando enviar peticion a Spring Boot...")

            try {
                // Get FCM Token before login
                var fcmToken: String? = null
                try {
                    val tokenResult = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                    fcmToken = tokenResult
                } catch (e: Exception) {
                    Log.e("AgroFlowLogin", "Error obteniendo FCM token: ${e.message}")
                }

                val request = LoginRequest(correo, clave, fcmToken)
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
                        
                        // Load Profile Data
                        try {
                            val profileResponse = RetrofitClient.userApi.getUserProfile(body.usuarioId)
                            if (profileResponse.isSuccessful) {
                                val p = profileResponse.body()
                                if (p != null) {
                                    com.agroflow.core.session.SessionManager.userName = p.nombre
                                    com.agroflow.core.session.SessionManager.userPhone = p.telefono
                                    com.agroflow.core.session.SessionManager.userAddress = p.direccion
                                    com.agroflow.core.session.SessionManager.userPhotoUri = p.fotoPerfilUrl
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AgroFlowLogin", "Error cargando perfil: ${e.message}")
                        }
                    }
                    uiState = LoginUiState.Success
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
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onError("Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }

    fun updateUserProfile(id: String, nombre: String, telefono: String, direccion: String, fotoUrl: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.agroflow.feature.auth.data.UpdateUserRequest(nombre, telefono, direccion, fotoUrl)
                val response = RetrofitClient.userApi.updateUser(id, request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onError("Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }
}