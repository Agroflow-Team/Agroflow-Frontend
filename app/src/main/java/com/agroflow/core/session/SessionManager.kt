package com.agroflow.core.session

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object SessionManager {
    var userId by mutableStateOf<String?>(null)
    var userEmail by mutableStateOf<String?>(null)
    var roleId by mutableStateOf<String?>(null)
    var token by mutableStateOf<String?>(null)
    var fincaId by mutableStateOf<String?>(null)
    
    // Configurable base URL for testing
    var baseUrl by mutableStateOf("http://192.168.0.144:8080/")

    fun saveSession(userId: String, email: String, roleId: String, token: String) {
        this.userId = userId
        this.userEmail = email
        this.roleId = roleId
        this.token = token
    }

    fun clearSession() {
        userId = null
        userEmail = null
        roleId = null
        token = null
        fincaId = null
    }

    fun isLoggedIn(): Boolean {
        return token != null
    }
}
