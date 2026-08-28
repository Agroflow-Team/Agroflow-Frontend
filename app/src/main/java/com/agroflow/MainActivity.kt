package com.agroflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import com.agroflow.core.theme.AgroFlowTheme
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.dashboard.presentation.ui.DashboardScreen
import com.agroflow.feature.auth.presentation.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        var isLoggedIn by remember { mutableStateOf(SessionManager.isLoggedIn()) }
                        
                        if (isLoggedIn) {
                            // Enrutamiento por rol
                            val roleId = SessionManager.roleId?.lowercase() ?: ""
                            val isEmpleado = roleId.contains("empleado") || roleId.contains("trabajador") || roleId == "2"
                            
                            if (isEmpleado) {
                                com.agroflow.feature.empleado.presentation.ui.EmpleadoDashboardScreen(
                                    onLogout = { isLoggedIn = false }
                                )
                            } else {
                                DashboardScreen(
                                    onLogout = { isLoggedIn = false }
                                )
                            }
                        } else {
                            LoginScreen(
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "Bienvenido a AgroFlow",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}