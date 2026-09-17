package com.agroflow.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object SessionManager {
    private var prefs: SharedPreferences? = null

    // Backing properties for Compose state
    private var _userId = mutableStateOf<String?>(null)
    private var _userEmail = mutableStateOf<String?>(null)
    private var _roleId = mutableStateOf<String?>(null)
    private var _token = mutableStateOf<String?>(null)
    private var _fincaId = mutableStateOf<String?>(null)
    
    private var _userName = mutableStateOf<String?>(null)
    private var _userPhone = mutableStateOf<String?>(null)
    private var _userAddress = mutableStateOf<String?>(null)
    private var _userPhotoUri = mutableStateOf<String?>(null)

    // Public properties
    var userId: String?
        get() = _userId.value
        set(value) {
            _userId.value = value
            prefs?.edit()?.putString("userId", value)?.apply()
        }
        
    var userEmail: String?
        get() = _userEmail.value
        set(value) {
            _userEmail.value = value
            prefs?.edit()?.putString("userEmail", value)?.apply()
        }
        
    var roleId: String?
        get() = _roleId.value
        set(value) {
            _roleId.value = value
            prefs?.edit()?.putString("roleId", value)?.apply()
        }
        
    var token: String?
        get() = _token.value
        set(value) {
            _token.value = value
            prefs?.edit()?.putString("token", value)?.apply()
        }
        
    var fincaId: String?
        get() = _fincaId.value
        set(value) {
            _fincaId.value = value
            prefs?.edit()?.putString("fincaId", value)?.apply()
        }

    var userName: String?
        get() = _userName.value
        set(value) {
            _userName.value = value
            prefs?.edit()?.putString("userName", value)?.apply()
        }
        
    var userPhone: String?
        get() = _userPhone.value
        set(value) {
            _userPhone.value = value
            prefs?.edit()?.putString("userPhone", value)?.apply()
        }
        
    var userAddress: String?
        get() = _userAddress.value
        set(value) {
            _userAddress.value = value
            prefs?.edit()?.putString("userAddress", value)?.apply()
        }
        
    var userPhotoUri: String?
        get() = _userPhotoUri.value
        set(value) {
            _userPhotoUri.value = value
            prefs?.edit()?.putString("userPhotoUri", value)?.apply()
        }

    const val ROLE_ADMIN = "ef5ee967-eb0c-491e-b77f-663dfc88510b"
    const val ROLE_AGRICULTOR = "6fe0a91a-1318-499e-bedf-6722914d61fd"
    const val ROLE_TRABAJADOR = "7896dd16-8aa2-4161-ba08-afe874300fe5"
    const val ROLE_CLIENTE = "a42f1efb-78bd-45e4-8a95-7b1c863663bf"

    // Configurable base URL for testing
    var baseUrl by mutableStateOf("https://agroflow-backend-sena.azurewebsites.net/")

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("AgroFlowSession", Context.MODE_PRIVATE)
            loadSession()
        }
    }

    private fun loadSession() {
        val savedTime = prefs?.getLong("loginTime", 0L) ?: 0L
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (savedTime > 0 && currentTime - savedTime < oneDayMillis) {
            _userId.value = prefs?.getString("userId", null)
            _userEmail.value = prefs?.getString("userEmail", null)
            _roleId.value = prefs?.getString("roleId", null)
            _token.value = prefs?.getString("token", null)
            _fincaId.value = prefs?.getString("fincaId", null)
            _userName.value = prefs?.getString("userName", null)
            _userPhone.value = prefs?.getString("userPhone", null)
            _userAddress.value = prefs?.getString("userAddress", null)
            _userPhotoUri.value = prefs?.getString("userPhotoUri", null)
        } else if (savedTime > 0) {
            clearSession()
        }
    }

    fun saveSession(userId: String, email: String, roleId: String, token: String) {
        this.userId = userId 
        this.userEmail = email
        this.roleId = roleId
        this.token = token
        
        prefs?.edit()?.putLong("loginTime", System.currentTimeMillis())?.apply()
    }

    fun clearSession() {
        _userId.value = null
        _userEmail.value = null
        _roleId.value = null
        _token.value = null
        _fincaId.value = null
        _userName.value = null
        _userPhone.value = null
        _userAddress.value = null
        _userPhotoUri.value = null
        
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean {
        return token != null
    }
}
