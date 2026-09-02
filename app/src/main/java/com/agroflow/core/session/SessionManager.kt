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
    const val ROLE_ADMIN = "ef5ee967-eb0c-491e-b77f-663dfc88510b"
    const val ROLE_AGRICULTOR = "6fe0a91a-1318-499e-bedf-6722914d61fd"
    const val ROLE_TRABAJADOR = "7896dd16-8aa2-4161-ba08-afe874300fe5"
    const val ROLE_CLIENTE = "a42f1efb-78bd-45e4-8a95-7b1c863663bf"

    // Configurable base URL for testing
    var baseUrl by mutableStateOf("https://agroflow-backend-1.onrender.com/")

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
